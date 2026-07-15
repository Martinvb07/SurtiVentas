# Supplier-invoice OCR microservice (Fase 2).
#
# A tiny, dependency-free HTTP wrapper (Python stdlib, no pip) around the
# tesseract binary. The backend POSTs raw file bytes and gets the extracted
# text back as JSON. poppler-utils is included so PDF invoices are rasterised
# (pdftoppm) before OCR; the Spanish + English trained data cover the invoices
# SurtiVentas handles.
FROM alpine:3.20

RUN apk add --no-cache \
    python3 \
    tesseract-ocr \
    tesseract-ocr-data-spa \
    tesseract-ocr-data-eng \
    poppler-utils

WORKDIR /app
COPY app.py .

EXPOSE 8000

# busybox wget (bundled in Alpine) backs the compose healthcheck against /health.
CMD ["python3", "app.py"]
