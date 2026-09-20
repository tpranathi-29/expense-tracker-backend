package com.example.expense_tracker.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SharedExpenseRequest(
        @NotBlank String description,
        @NotNull @Positive BigDecimal totalAmount,
        @NotNull LocalDate date,
        @NotBlank String payerEmail,
        @NotEmpty List<@Valid ShareRequest> shares) {

    public record ShareRequest(
            @NotBlank String userEmail,
            @NotNull @Positive BigDecimal amount) {
    }
}
