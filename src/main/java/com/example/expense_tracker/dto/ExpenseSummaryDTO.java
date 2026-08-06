package com.example.expense_tracker.dto;

public class ExpenseSummaryDTO {

    private double totalExpense;
    private int totalTransactions;

    public ExpenseSummaryDTO() {
    }

    public ExpenseSummaryDTO(double totalExpense, int totalTransactions) {
        this.totalExpense = totalExpense;
        this.totalTransactions = totalTransactions;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(double totalExpense) {
        this.totalExpense = totalExpense;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(int totalTransactions) {
        this.totalTransactions = totalTransactions;
    }
}