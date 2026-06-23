#!/usr/bin/env bash
set -euo pipefail

# This script runs a local sample request against a server on http://localhost:8080
# It reads inputs from variables.json in the project root.

# Resolve repository root (directory containing this script is scripts/)
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="${SCRIPT_DIR%/scripts}"
cd "$REPO_ROOT"

# Check dependencies
for bin in curl jq; do
  if ! command -v "$bin" >/dev/null 2>&1; then
    echo "Error: required tool '$bin' not found in PATH" >&2
    exit 1
  fi
done

if [[ ! -f variables.json ]]; then
  echo "Error: variables.json not found at $REPO_ROOT/variables.json" >&2
  exit 1
fi

BOOK=$(jq -r '.book_full_path // empty' variables.json)
OUT=$(jq -r '.output_name // empty' variables.json)
PARTS=$(jq -r '.parts // empty' variables.json)

if [[ -z "$BOOK" || -z "$OUT" || -z "$PARTS" ]]; then
  echo "Error: variables.json must contain 'book_full_path', 'output_name', and 'parts'" >&2
  exit 1
fi

if [[ ! -f "$BOOK" ]]; then
  echo "Error: book file not found: $BOOK" >&2
  exit 1
fi

if [[ ! -f "$PARTS" ]]; then
  echo "Error: parts file not found: $PARTS" >&2
  exit 1
fi

API_BASE="http://localhost:8080"

# Work around curl -F parsing issues with filenames containing spaces/commas
# by creating temporary copies with safe names when needed.
TMPDIR=""
cleanup() { if [[ -n "$TMPDIR" && -d "$TMPDIR" ]]; then rm -rf "$TMPDIR"; fi; }
trap cleanup EXIT

UPLOAD_BOOK="$BOOK"
UPLOAD_PARTS="$PARTS"

if [[ "$BOOK" =~ [[:space:],] ]]; then
  TMPDIR=$(mktemp -d)
  cp -f "${BOOK}" "${TMPDIR}/book.pdf"
  UPLOAD_BOOK="${TMPDIR}/book.pdf"
fi

if [[ "$PARTS" =~ [[:space:],] ]]; then
  [[ -z "$TMPDIR" ]] && TMPDIR=$(mktemp -d)
  cp -f "${PARTS}" "${TMPDIR}/parts.json"
  UPLOAD_PARTS="${TMPDIR}/parts.json"
fi

echo "Waiting for API at $API_BASE ..."
ATTEMPTS=0
until curl -sf "$API_BASE/v3/api-docs" >/dev/null 2>&1; do
  ATTEMPTS=$((ATTEMPTS+1))
  if (( ATTEMPTS > 120 )); then
    echo "Error: API did not become ready within 120 seconds" >&2
    exit 1
  fi
  sleep 1
done

echo "Using: BOOK=$BOOK (upload: $UPLOAD_BOOK) OUT=$OUT PARTS=$PARTS (upload: $UPLOAD_PARTS)"

set -x
curl -sS -f -o "$OUT" \
  -F "file=@${UPLOAD_BOOK};type=application/pdf" \
  -F "parts=@${UPLOAD_PARTS};type=application/json" \
  "$API_BASE/api/pdf/split-manual-title"
set +x

echo "Saved ZIP => $OUT"
if command -v sha256sum >/dev/null 2>&1; then
  echo -n "SHA256: "
  sha256sum "$OUT" | awk '{print $1}'
fi
