# PDF Splitter API

A Spring Boot REST API that receives a PDF file and splits it every 50 pages into new files named `{originalName}(pageStart-pageEnd).pdf`.

## Requirements

- Java 17+
- Maven 3.6+

## Run the Application

```bash
mvn spring-boot:run
```

The server starts on `http://localhost:8080`.

## API Usage

### 1. Split PDF (Every 50 Pages)

**Endpoint:** `POST /api/pdf/split`

**Request:** Multipart form data with a PDF file (form field name: `file`)

**Response:** Returns a ZIP file containing split PDFs named `{originalName}(pageStart-pageEnd).pdf`.

**Example with cURL:**

```bash
curl -X POST -F "file=@/path/to/document.pdf" \
  -o output.zip \
  http://localhost:8080/api/pdf/split
```

### 2. Manual Split by Page Numbers

**Endpoint:** `POST /api/pdf/split-manual`

**Request:** 
- `file`: PDF file to split.
- `pageSplitNumber`: List of page numbers where the PDF should be split.

**Example with cURL:**

```bash
curl -X POST -F "file=@/path/to/document.pdf" \
  -F "pageSplitNumber=10,20,30" \
  -o output.zip \
  http://localhost:8080/api/pdf/split-manual
```

### 3. Manual Split with Titles and Page Ranges

**Endpoint:** `POST /api/pdf/split-manual-title`

**Request:**
- `file`: PDF file to split.
- `parts`: JSON array of objects with `title`, `startPage`, and `endPage`.

**Example with cURL:**

```bash
curl -X POST \
  -F "file=@/path/to/document.pdf" \
  -F 'parts=[{"title":"Introduction","startPage":1,"endPage":5},{"title":"Chapter1","startPage":6,"endPage":20}];type=application/json' \
  -o output.zip \
  http://localhost:8080/api/pdf/split-manual-title
```

## Output Examples

- `document.pdf` (120 pages) split every 50 pages → `document(1-50).pdf`, `document(51-100).pdf`, `document(101-120).pdf`
- `document.pdf` (100 pages) split at page 10, 20 → `document(1-9).pdf`, `document(10-19).pdf`, `document(20-100).pdf`

## Configuration

- Max file size: 100MB (configurable in `application.properties`)
