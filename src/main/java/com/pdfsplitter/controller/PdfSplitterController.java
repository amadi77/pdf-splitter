package com.pdfsplitter.controller;

import com.pdfsplitter.service.PdfSplitterService;
import com.pdfsplitter.service.SplitResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/pdf")
@Tag(name = "PDF Splitter", description = "API for splitting PDF files every 50 pages")
public class PdfSplitterController {

    private final PdfSplitterService pdfSplitterService;

    public PdfSplitterController(PdfSplitterService pdfSplitterService) {
        this.pdfSplitterService = pdfSplitterService;
    }

    @Operation(summary = "Split PDF", description = "Upload a PDF file to split it every 50 pages. Returns a ZIP file containing all split PDFs named {originalName}(pageStart-pageEnd).pdf")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully split PDF(s)"),
            @ApiResponse(responseCode = "400", description = "Invalid file (empty or not a PDF)")
    })
    @PostMapping(value = "/split", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> splitPdf(
            @RequestPart("file") @Parameter(description = "PDF file to split", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)) MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "document.pdf";
        }
        String baseName = originalFilename.toLowerCase().endsWith(".pdf")
                ? originalFilename.replaceAll("(?i)\\.pdf$", "")
                : originalFilename;

        List<SplitResult> splitResults = pdfSplitterService.splitPdf(file);

        try (ByteArrayOutputStream zipStream = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(zipStream)) {

            for (SplitResult result : splitResults) {
                ZipEntry entry = new ZipEntry(result.filename());
                zos.putNextEntry(entry);
                zos.write(result.content());
                zos.closeEntry();
            }

            zos.finish();
            byte[] zipBytes = zipStream.toByteArray();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + baseName + "-split.zip\"")
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(zipBytes);
        }
    }


    @Operation(summary = "Split PDF", description = "Upload a PDF file to split it using splitNumberPage. Returns a ZIP file containing all split PDFs named {originalName}(pageStart-pageEnd).pdf")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully split PDF(s)"),
            @ApiResponse(responseCode = "400", description = "Invalid file (empty or not a PDF)")
    })
    @PostMapping(value = "/split-manual", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> splitPdfWithPageNumber(
            @RequestPart("file") @Parameter(description = "PDF file to split", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)) MultipartFile file,
            @RequestParam List<Integer> pageSplitNumber) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "document.pdf";
        }
        String baseName = originalFilename.toLowerCase().endsWith(".pdf")
                ? originalFilename.replaceAll("(?i)\\.pdf$", "")
                : originalFilename;

        List<SplitResult> splitResults = pdfSplitterService.splitPdf(file, pageSplitNumber);

        try (ByteArrayOutputStream zipStream = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(zipStream)) {

            for (SplitResult result : splitResults) {
                ZipEntry entry = new ZipEntry(result.filename());
                zos.putNextEntry(entry);
                zos.write(result.content());
                zos.closeEntry();
            }

            zos.finish();
            byte[] zipBytes = zipStream.toByteArray();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + baseName + "-split.zip\"")
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(zipBytes);
        }
    }

    @Operation(
            summary = "Split PDF by Page Ranges",
            description = "Upload a PDF and a JSON list of page ranges with titles. Send 'file' as the PDF and 'parts' as application/json. Returns a ZIP file containing the split sections."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully generated ZIP containing split PDFs"),
            @ApiResponse(responseCode = "400", description = "Invalid file or malformed split ranges")
    })
    @PostMapping(value = "/split-manual-title", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> splitPdfWithManualTitle(
            @RequestPart("file") @Parameter(description = "PDF file to split", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)) MultipartFile file,
            @RequestPart("parts") @Parameter(description = "JSON array of {title, startPage, endPage}", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) List<PdfSplitItem> parts) throws IOException {

        if (file.isEmpty() || file.getContentType() == null || !file.getContentType().equalsIgnoreCase("application/pdf")) {
            return ResponseEntity.badRequest().build();
        }

        PdfSplitRequest request = new PdfSplitRequest(file, parts);
        String originalFilename = request.file().getOriginalFilename();
        String baseName = (originalFilename != null && originalFilename.toLowerCase().endsWith(".pdf"))
                ? originalFilename.substring(0, originalFilename.length() - 4)
                : "document";

        List<SplitResult> splitResults = pdfSplitterService.splitPdfParts(request);

        try (ByteArrayOutputStream zipStream = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(zipStream)) {

            for (SplitResult result : splitResults) {
                // result.filename() should ideally be "{originalName}({title}).pdf"
                // as per your description requirements
                ZipEntry entry = new ZipEntry(result.filename());
                zos.putNextEntry(entry);
                zos.write(result.content());
                zos.closeEntry();
            }

            zos.finish();
            byte[] zipBytes = zipStream.toByteArray();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + baseName + "-split.zip\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/zip")
                    .body(zipBytes);
        }
    }
}
