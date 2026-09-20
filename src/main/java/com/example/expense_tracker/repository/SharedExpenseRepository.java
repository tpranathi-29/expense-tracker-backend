package com.example.expense_tracker.repository;

import com.example.expense_tracker.model.SharedExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SharedExpenseRepository extends JpaRepository<SharedExpense, Long> {
    List<SharedExpense> findByGroupIdOrderByDateDescIdDesc(Long groupId);
}
