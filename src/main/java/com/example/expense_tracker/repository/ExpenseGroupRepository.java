package com.example.expense_tracker.repository;

import com.example.expense_tracker.model.ExpenseGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseGroupRepository extends JpaRepository<ExpenseGroup, Long> {
    List<ExpenseGroup> findDistinctByMembersEmailOrderByNameAsc(String email);
}
