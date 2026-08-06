package com.example.expense_tracker.repository;

import com.example.expense_tracker.dto.MonthlyExpenseProjection;
import com.example.expense_tracker.model.Expense;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // ==========================================================
    // User Queries
    // ==========================================================

    List<Expense> findByUserEmail(String email);

    Page<Expense> findByUserEmail(
            String email,
            Pageable pageable
    );

    // ==========================================================
    // Category Queries
    // ==========================================================

    List<Expense> findByCategory(String category);

    List<Expense> findByCategoryAndUserEmail(
            String category,
            String email
    );

    // ==========================================================
    // Search Queries
    // ==========================================================

    List<Expense> findByTitleContainingIgnoreCase(String keyword);

    List<Expense> findByTitleContainingIgnoreCaseAndUserEmail(
            String title,
            String email
    );

    @Query("""
            SELECT e
            FROM Expense e
            WHERE e.user.email = :email
            AND (
                LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            """)
    List<Expense> searchExpenses(
            @Param("keyword") String keyword,
            @Param("email") String email
    );

    // ==========================================================
    // Dashboard Summary
    // ==========================================================

    @Query("""
            SELECT COALESCE(SUM(e.amount),0)
            FROM Expense e
            WHERE e.user.email=:email
            """)
    Double getTotalAmount(
            @Param("email") String email
    );

    @Query("""
            SELECT COUNT(e)
            FROM Expense e
            WHERE e.user.email=:email
            """)
    Long getTotalTransactions(
            @Param("email") String email
    );

    @Query("""
            SELECT COUNT(DISTINCT e.category)
            FROM Expense e
            WHERE e.user.email=:email
            """)
    Long getTotalCategories(
            @Param("email") String email
    );

    // ==========================================================
    // Monthly Analytics
    // ==========================================================

    @Query("""
            SELECT
            FUNCTION('DATE_FORMAT',e.date,'%Y-%m') AS month,
            SUM(e.amount) AS totalAmount
            FROM Expense e
            WHERE e.user.email=:email
            GROUP BY FUNCTION('DATE_FORMAT',e.date,'%Y-%m')
            ORDER BY FUNCTION('DATE_FORMAT',e.date,'%Y-%m')
            """)
    List<MonthlyExpenseProjection> getMonthlyExpenses(
            @Param("email") String email
    );

}