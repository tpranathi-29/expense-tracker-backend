package com.example.expense_tracker.controller;

import com.example.expense_tracker.dto.ExpenseSummaryDTO;
import com.example.expense_tracker.dto.MonthlyExpenseProjection;
import com.example.expense_tracker.model.Expense;
import com.example.expense_tracker.service.ExpenseService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/expenses")
@Tag(name = "Expense Controller")
@SecurityRequirement(name = "Bearer Authentication")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // =====================================================
    // Add Expense
    // =====================================================

    @PostMapping
    public ResponseEntity<Expense> addExpense(
            @Valid @RequestBody Expense expense,
            Authentication authentication) {

        String email = authentication.getName();

        Expense savedExpense = expenseService.addExpense(expense, email);

        return ResponseEntity.ok(savedExpense);
    }

    // =====================================================
    // Get All Expenses
    // =====================================================

    @GetMapping
    public ResponseEntity<List<Expense>> getAllExpenses(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                expenseService.getAllExpenses(email)
        );
    }

    // =====================================================
    // Pagination
    // =====================================================

    @GetMapping("/page")
    public ResponseEntity<Page<Expense>> getExpensesPage(
            Pageable pageable,
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                expenseService.getExpenses(email, pageable)
        );
    }

    // =====================================================
    // Update Expense
    // =====================================================

    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody Expense expense,
            Authentication authentication) {

        String email = authentication.getName();

        Expense updatedExpense =
                expenseService.updateExpense(
                        id,
                        expense,
                        email
                );

        return ResponseEntity.ok(updatedExpense);
    }

    // =====================================================
    // Delete Expense
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteExpense(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        expenseService.deleteExpense(id, email);

        return ResponseEntity.ok(
                "Expense deleted successfully!"
        );
    }

    // =====================================================
    // Dashboard Summary
    // =====================================================

    @GetMapping("/summary")
    public ResponseEntity<ExpenseSummaryDTO> getSummary(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                expenseService.getSummary(email)
        );
    }

    // =====================================================
    // Category Filter
    // =====================================================

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Expense>> getByCategory(
            @PathVariable String category,
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                expenseService.getByCategory(
                        category,
                        email
                )
        );
    }

    // =====================================================
    // Search Expenses
    // =====================================================

    @GetMapping("/search")
    public ResponseEntity<List<Expense>> searchExpenses(
            @RequestParam String keyword,
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                expenseService.searchExpenses(
                        keyword,
                        email
                )
        );
    }

    // =====================================================
    // Monthly Expense Analytics
    // =====================================================

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyExpenseProjection>> getMonthlyExpenses(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                expenseService.getMonthlyExpenses(email)
        );
    }

    // =====================================================
    // Upload Receipt
    // =====================================================

    @PostMapping("/{id}/receipt")
    public ResponseEntity<String> uploadReceipt(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        String email = authentication.getName();

        expenseService.uploadReceipt(
                id,
                file,
                email
        );

        return ResponseEntity.ok(
                "Receipt uploaded successfully!"
        );
    }

    // =====================================================
    // Download PDF Report
    // =====================================================

    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> downloadPdfReport(
            Authentication authentication) {

        String email = authentication.getName();

        byte[] pdf =
                expenseService.generatePdfReport(email);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Expense_Report.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}
