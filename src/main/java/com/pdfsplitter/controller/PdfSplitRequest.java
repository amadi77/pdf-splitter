package com.pdfsplitter.controller;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public record PdfSplitRequest(MultipartFile file,List<PdfSplitItem> parts) {

}
