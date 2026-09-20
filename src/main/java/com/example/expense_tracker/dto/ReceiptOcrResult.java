package com.example.expense_tracker.dto;

public record ReceiptOcrResult(
        String fileName,
        String merchant,
        Double amount,
        String rawText,
        String message) {
}