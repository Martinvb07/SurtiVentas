# Placeholder Tesseract OCR service.
#
# This container only proves the docker-compose topology for the future
# supplier-invoice OCR module (Fase 2). It currently exposes no HTTP API —
# a later session adds a small web wrapper (e.g. Flask/FastAPI or a tiny
# Spring Boot service) around the tesseract binary and the backend calling
# code that talks to it.
FROM alpine:3.20

RUN apk add --no-cache tesseract-ocr tesseract-ocr-data-spa tesseract-ocr-data-eng

CMD ["tail", "-f", "/dev/null"]
