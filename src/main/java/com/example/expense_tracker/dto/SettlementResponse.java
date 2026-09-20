package com.example.expense_tracker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementResponse(
        Long id,
        String fromUserEmail,
        String toUserEmail,
        BigDecimal amount,
        String note,
        LocalDateTime createdAt) {
}
