package com.example.expense_tracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SharedExpenseResponse(
        Long id,
        String description,
        BigDecimal totalAmount,
        LocalDate date,
        Member payer,
        List<Share> shares) {

    public record Member(Long id, String name, String email) {
    }

    public record Share(Member user, BigDecimal amount) {
    }
}
