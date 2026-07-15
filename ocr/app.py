"""
Thin OCR HTTP wrapper around the Tesseract binary for the supplier-invoice
module (Fase 2).

Kept deliberately dependency-free: it uses only the Python standard library
(no Flask/FastAPI, no pip install) so the Alpine image stays small and builds
reliably. The backend POSTs the raw file bytes with the file's Content-Type;
this service shells out to `tesseract` (and to `pdftoppm` first when the upload
is a PDF) and returns the extracted text as JSON.

Contract
--------
GET  /health          -> 200 {"status": "ok"}
POST /ocr             -> 200 {"text": "...", "lang": "spa+eng"}
    Request body: raw file bytes.
    Header Content-Type: image/png | image/jpeg | image/jpg | application/pdf
    On a bad request: 400 {"error": "..."}; on a processing failure: 500.
"""

import json
import os
import subprocess
import sys
import tempfile
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

PORT = int(os.environ.get("OCR_PORT", "8000"))
LANGS = os.environ.get("OCR_LANGS", "spa+eng")
# Guardrail so a runaway upload can't exhaust memory. Matches the backend's
# multipart limit (15 MB) with a little headroom.
MAX_BYTES = int(os.environ.get("OCR_MAX_BYTES", str(20 * 1024 * 1024)))
TESSERACT_TIMEOUT_S = int(os.environ.get("OCR_TIMEOUT_S", "60"))

SUFFIX_BY_CONTENT_TYPE = {
    "image/png": ".png",
    "image/jpeg": ".jpg",
    "image/jpg": ".jpg",
    "application/pdf": ".pdf",
}


def _run(cmd, **kwargs):
    return subprocess.run(
        cmd,
        capture_output=True,
        timeout=TESSERACT_TIMEOUT_S,
        check=True,
        **kwargs,
    )


def _pdf_to_png(pdf_path):
    """Rasterise the PDF's first page to PNG via poppler's pdftoppm."""
    out_base = pdf_path[: -len(".pdf")]
    _run(["pdftoppm", "-png", "-r", "200", "-singlefile", pdf_path, out_base])
    return out_base + ".png"


def _ocr_image(image_path):
    result = _run(["tesseract", image_path, "stdout", "-l", LANGS])
    return result.stdout.decode("utf-8", errors="replace")


def extract_text(raw_bytes, content_type):
    suffix = SUFFIX_BY_CONTENT_TYPE.get((content_type or "").split(";")[0].strip().lower())
    if suffix is None:
        raise ValueError(
            "Unsupported content type '%s'; expected an image (PNG/JPEG) or PDF" % content_type
        )

    tmp_dir = tempfile.mkdtemp(prefix="ocr-")
    src_path = os.path.join(tmp_dir, "upload" + suffix)
    with open(src_path, "wb") as fh:
        fh.write(raw_bytes)

    image_path = _pdf_to_png(src_path) if suffix == ".pdf" else src_path
    return _ocr_image(image_path)


class OcrHandler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"

    def _send_json(self, status, payload):
        body = json.dumps(payload).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        if self.path == "/health":
            self._send_json(200, {"status": "ok", "lang": LANGS})
        else:
            self._send_json(404, {"error": "not found"})

    def do_POST(self):
        if self.path != "/ocr":
            self._send_json(404, {"error": "not found"})
            return

        length = int(self.headers.get("Content-Length", "0"))
        if length <= 0:
            self._send_json(400, {"error": "empty body"})
            return
        if length > MAX_BYTES:
            self._send_json(413, {"error": "file too large"})
            return

        raw = self.rfile.read(length)
        content_type = self.headers.get("Content-Type", "")
        try:
            text = extract_text(raw, content_type)
        except ValueError as exc:
            self._send_json(400, {"error": str(exc)})
        except subprocess.TimeoutExpired:
            self._send_json(500, {"error": "OCR timed out"})
        except subprocess.CalledProcessError as exc:
            detail = exc.stderr.decode("utf-8", errors="replace") if exc.stderr else str(exc)
            self._send_json(500, {"error": "OCR failed", "detail": detail})
        else:
            self._send_json(200, {"text": text, "lang": LANGS})

    # Quieter, single-line access logging.
    def log_message(self, fmt, *args):
        sys.stderr.write("ocr %s - %s\n" % (self.address_string(), fmt % args))


def main():
    server = ThreadingHTTPServer(("0.0.0.0", PORT), OcrHandler)
    sys.stderr.write("OCR service listening on :%d (langs=%s)\n" % (PORT, LANGS))
    server.serve_forever()


if __name__ == "__main__":
    main()
