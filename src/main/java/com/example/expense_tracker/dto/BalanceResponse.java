package com.example.expense_tracker.dto;

import java.math.BigDecimal;

public record BalanceResponse(
        String debtorEmail,
        String debtorName,
        String creditorEmail,
        String creditorName,
        BigDecimal amount) {
}
