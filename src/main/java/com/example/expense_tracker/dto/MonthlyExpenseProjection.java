package com.example.expense_tracker.dto;

public interface MonthlyExpenseProjection {

    String getMonth();

    Double getTotalAmount();
}