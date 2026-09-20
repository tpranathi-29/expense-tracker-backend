package com.example.expense_tracker.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReceiptOcrService {

    private static final Pattern MONEY_PATTERN = Pattern.compile(
            "(?i)(?:total|amount|grand total|balance|due)?\\s*[:#-]?\\s*[$€£]?\\s*(\\d{1,3}(?:[, ]\\d{3})*(?:\\.\\d{2})|\\d+\\.\\d{2})");

    public OcrData extract(Path imagePath) {
        try {
            Tesseract tesseract = new Tesseract();
            String dataPath = System.getenv("TESSDATA_PREFIX");
            if (dataPath != null && !dataPath.isBlank()) {
                tesseract.setDatapath(dataPath);
            }
            String rawText = tesseract.doOCR(imagePath.toFile()).trim();
            return new OcrData(rawText, findMerchant(rawText), findAmount(rawText), null);
        } catch (TesseractException | RuntimeException exception) {
            return new OcrData("", null, null,
                    "Receipt uploaded, but OCR could not extract text. Configure TESSDATA_PREFIX on the server.");
        }
    }

    private String findMerchant(String text) {
        return text.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank() && !line.matches(".*\\d.*"))
                .findFirst()
                .orElse(null);
    }

    private Double findAmount(String text) {
        Matcher matcher = MONEY_PATTERN.matcher(text);
        Double lastAmount = null;
        while (matcher.find()) {
            try {
                lastAmount = Double.parseDouble(matcher.group(1).replace(",", "").replace(" ", ""));
            } catch (NumberFormatException ignored) {
            }
        }
        return lastAmount;
    }

    public record OcrData(String rawText, String merchant, Double amount, String message) {
    }
}