#!/bin/sh
set -ex

# Ensure required tools are available
apk add --no-cache curl jq >/dev/null

cd /workspace

echo "Waiting for API..."
until curl -sf http://app:8080/v3/api-docs >/dev/null; do sleep 1; done

BOOK=$(jq -r .book_full_path variables.json)
OUT=$(jq -r .output_name variables.json)
PARTS=$(jq -r .parts variables.json)

echo "Using: BOOK=$BOOK OUT=$OUT PARTS=$PARTS"

curl -s -f -o "$OUT" \
  -F "file=@${BOOK};type=application/pdf" \
  -F "parts=@${PARTS};type=application/json" \
  http://app:8080/api/pdf/split-manual-title

echo "Saved ZIP => $OUT"

# keep container alive briefly so logs are viewable
sleep 5
