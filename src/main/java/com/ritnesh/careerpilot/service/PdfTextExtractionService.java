package com.ritnesh.careerpilot.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class PdfTextExtractionService {

    /**
     * Extracts plain text from a PDF file on disk.
     * Truncates very long resumes to keep prompt size (and cost) bounded.
     */
    public String extractText(Path pdfPath) throws IOException {

        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {

            if (document.isEncrypted()) {
                throw new IOException("Cannot extract text from a password-protected PDF.");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            int maxChars = 12000; // keeps the prompt within a sane token budget
            if (text.length() > maxChars) {
                text = text.substring(0, maxChars);
            }

            return text.trim();
        }
    }
}
