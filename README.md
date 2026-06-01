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
curl --location 'http://localhost:8080/api/pdf/split-manual-title' 
--form 'file=@"path-to-book"' 
--form 'parts="[{\"title\": \"Chapter 1: Beyond the Getting-Started Guide\", \"startPage\": 1, \"endPage\": 10}, {\"title\": \"Chapter 2: The Permission and Trust Architecture\", \"startPage\": 10, \"endPage\": 20}, {\"title\": \"Chapter 3: Context Engineering\", \"startPage\": 20, \"endPage\": 30}, {\"title\": \"Chapter 4: Multi-Agent Orchestration\", \"startPage\": 30, \"endPage\": 44}, {\"title\": \"Chapter 5: MCP -- Connecting Claude Code to Everything\", \"startPage\": 44, \"endPage\": 52}, {\"title\": \"Chapter 6: CI/CD and Headless Automation\", \"startPage\": 52, \"endPage\": 63}, {\"title\": \"Chapter 7: IDE Integration Done Right\", \"startPage\": 63, \"endPage\": 71}, {\"title\": \"Chapter 8: Prompt Craft for Agentic Tools\", \"startPage\": 71, \"endPage\": 82}, {\"title\": \"Chapter 9: Working with Large and Legacy Codebases\", \"startPage\": 82, \"endPage\": 89}, {\"title\": \"Chapter 10: Failure Modes and Recovery\", \"startPage\": 90, \"endPage\": 99}, {\"title\": \"Chapter 11: Team Adoption Patterns\", \"startPage\": 99, \"endPage\": 110}, {\"title\": \"Chapter 12: The Economics and Strategy of AI-Assisted\", \"startPage\": 110, \"endPage\": 121}, {\"title\": \"Appendix A: Command Reference\", \"startPage\": 121, \"endPage\": 130}, {\"title\": \"Appendix B: Configuration Reference\", \"startPage\": 130, \"endPage\": 148}]";type=application/json' 
-o 'output-name.zip'

```

## Output Examples

- `document.pdf` (120 pages) split every 50 pages → `document(1-50).pdf`, `document(51-100).pdf`, `document(101-120).pdf`
- `document.pdf` (100 pages) split at page 10, 20 → `document(1-9).pdf`, `document(10-19).pdf`, `document(20-100).pdf`

## Configuration

- Max file size: 100MB (configurable in `application.properties`)
