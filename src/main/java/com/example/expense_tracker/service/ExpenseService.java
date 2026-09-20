package com.example.expense_tracker.service;

import com.example.expense_tracker.dto.ExpenseSummaryDTO;
import com.example.expense_tracker.dto.MonthlyExpenseProjection;
import com.example.expense_tracker.dto.ReceiptOcrResult;
import com.example.expense_tracker.exception.ResourceNotFoundException;
import com.example.expense_tracker.model.Expense;
import com.example.expense_tracker.model.User;
import com.example.expense_tracker.pdf.PdfService;
import com.example.expense_tracker.repository.ExpenseRepository;
import com.example.expense_tracker.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final PdfService pdfService;
    private final ReceiptOcrService receiptOcrService;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            UserRepository userRepository,
            PdfService pdfService,
            ReceiptOcrService receiptOcrService) {

        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.pdfService = pdfService;
        this.receiptOcrService = receiptOcrService;
    }

    // =====================================================
    // Add Expense
    // =====================================================

    public Expense addExpense(
            Expense expense,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        expense.setUser(user);

        return expenseRepository.save(expense);
    }

    // =====================================================
    // Get All Expenses
    // =====================================================

    public List<Expense> getAllExpenses(String email) {

        return expenseRepository.findByUserEmail(email);
    }

    // =====================================================
    // Pagination
    // =====================================================

    public Page<Expense> getExpenses(
            String email,
            Pageable pageable) {

        return expenseRepository.findByUserEmail(
                email,
                pageable
        );
    }

    // =====================================================
    // Get Expense By Id
    // =====================================================

    public Expense getExpenseById(
            Long id,
            String email) {

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expense not found"));

        if (!expense.getUser().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Access denied");
        }

        return expense;
    }

    // =====================================================
    // Update Expense
    // =====================================================

    public Expense updateExpense(
            Long id,
            Expense updatedExpense,
            String email) {

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expense not found"));

        if (!expense.getUser().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Access denied");
        }

        expense.setTitle(updatedExpense.getTitle());
        expense.setAmount(updatedExpense.getAmount());
        expense.setCategory(updatedExpense.getCategory());
        expense.setType(updatedExpense.getType());
        expense.setDate(updatedExpense.getDate());
        expense.setDescription(updatedExpense.getDescription());

        return expenseRepository.save(expense);
    }

    // =====================================================
    // Delete Expense
    // =====================================================

    public void deleteExpense(
            Long id,
            String email) {

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expense not found"));

        if (!expense.getUser().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Access denied");
        }

        expenseRepository.delete(expense);
    }
        // =====================================================
    // Dashboard Summary
    // =====================================================

    public ExpenseSummaryDTO getSummary(String email) {

        Double totalAmount = expenseRepository.getTotalAmount(email);

        Long totalTransactions =
                expenseRepository.getTotalTransactions(email);

        return new ExpenseSummaryDTO(
                totalAmount == null ? 0.0 : totalAmount,
                totalTransactions == null ? 0 : totalTransactions.intValue()
        );
    }

    // =====================================================
    // Category Filter
    // =====================================================

    public List<Expense> getByCategory(
            String category,
            String email) {

        return expenseRepository.findByCategoryAndUserEmail(
                category,
                email
        );
    }

    // =====================================================
    // Search Expenses
    // =====================================================

    public List<Expense> searchExpenses(
            String keyword,
            String email) {

        return expenseRepository.searchExpenses(
                keyword,
                email
        );
    }

    // =====================================================
    // Monthly Expense Analytics
    // =====================================================

    public List<MonthlyExpenseProjection> getMonthlyExpenses(
            String email) {

        return expenseRepository.getMonthlyExpenses(email);
    }
        // =====================================================
    // Upload Receipt
    // =====================================================

    public ReceiptOcrResult uploadReceipt(
            Long id,
            MultipartFile file,
            String email) {

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expense not found"));

        if (!expense.getUser().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Access denied");
        }

        try {

            Path uploadPath = Paths.get("uploads");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = System.currentTimeMillis()
                    + "_"
                    + file.getOriginalFilename();

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            expense.setReceipt(fileName);

            expenseRepository.save(expense);

                ReceiptOcrService.OcrData ocr = receiptOcrService.extract(filePath);
                return new ReceiptOcrResult(
                    fileName,
                    ocr.merchant(),
                    ocr.amount(),
                    ocr.rawText(),
                    ocr.message() == null ? "Receipt uploaded and analyzed." : ocr.message());

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to upload receipt.",
                    e
            );
        }
    }

    // =====================================================
    // Generate PDF Report
    // =====================================================

    public byte[] generatePdfReport(String email) {

        List<Expense> expenses =
                expenseRepository.findByUserEmail(email);

        return pdfService.generateExpenseReport(
                expenses,
                email
        );
    }

}