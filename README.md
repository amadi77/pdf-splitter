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

**Endpoint:** `POST /api/pdf/split`

**Request:** Multipart form data with a PDF file (form field name: `file`)

**Response:** Always returns a ZIP file containing all split PDFs named `{originalName}(pageStart-pageEnd).pdf`

**Example with cURL:**

```bash
curl -X POST -F "file=@/path/to/your/document.pdf" \
  -o output.zip \
  http://localhost:8080/api/pdf/split
```

**Output naming examples:**
- `document.pdf` (120 pages) → `document(1-50).pdf`, `document(51-100).pdf`, `document(101-120).pdf`

## Configuration

- Max file size: 100MB (configurable in `application.properties`)
