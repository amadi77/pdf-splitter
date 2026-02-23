package com.pdfsplitter.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfSplitterService {

    private static final int PAGES_PER_SPLIT = 50;

    public List<SplitResult> splitPdf(MultipartFile file) throws IOException {
        String baseName = getBaseName(file.getOriginalFilename());

        byte[] pdfBytes = file.getBytes();
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            List<SplitResult> result = new ArrayList<>();

            Splitter splitter = new Splitter();
            splitter.setSplitAtPage(PAGES_PER_SPLIT);

            List<PDDocument> splitDocuments = splitter.split(document);

            int pageStart = 1;
            for (PDDocument splitDoc : splitDocuments) {
                try {
                    int pageEnd = pageStart + splitDoc.getNumberOfPages() - 1;
                    String filename = baseName + "(" + pageStart + "-" + pageEnd + ").pdf";
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    splitDoc.save(outputStream);
                    result.add(new SplitResult(filename, outputStream.toByteArray()));
                    pageStart = pageEnd + 1;
                } finally {
                    splitDoc.close();
                }
            }

            return result;
        }
    }

    public List<SplitResult> splitPdf(MultipartFile file, List<Integer> pageSplitNumber) throws IOException {
        String baseName = getBaseName(file.getOriginalFilename());

        byte[] pdfBytes = file.getBytes();
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            List<SplitResult> result = new ArrayList<>();
            for (int i = 0; i+1 < pageSplitNumber.size(); i++) {
                int pageStart = pageSplitNumber.get(i);
                int pageEnd = pageSplitNumber.get(i + 1);
                Splitter splitter = new Splitter();
                splitter.setStartPage(pageStart);
                splitter.setEndPage(pageSplitNumber.get(i + 1));
                splitter.setSplitAtPage(pageEnd);

                List<PDDocument> splitDocuments = splitter.split(document);

                for (PDDocument splitDoc : splitDocuments) {
                    try {
                        String filename = baseName + "(" + pageStart + "-" + pageEnd + ").pdf";
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        splitDoc.save(outputStream);
                        result.add(new SplitResult(filename, outputStream.toByteArray()));
                    } finally {
                        splitDoc.close();
                    }
                }
            }

            return result;
        }
    }

    private String getBaseName(String filename) {
        if (filename == null || filename.isBlank()) {
            return "document";
        }
        String name = filename;
        int lastSlash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        if (lastSlash >= 0) {
            name = filename.substring(lastSlash + 1);
        }
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(0, lastDot) : name;
    }
}
