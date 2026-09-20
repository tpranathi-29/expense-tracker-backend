package com.example.expense_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SettlementRequest(
        @NotBlank String toUserEmail,
        @NotNull @Positive BigDecimal amount,
        String note) {
}
