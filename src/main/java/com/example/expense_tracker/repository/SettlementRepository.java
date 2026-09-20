package com.example.expense_tracker.repository;

import com.example.expense_tracker.model.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByGroupIdOrderByCreatedAtDesc(Long groupId);
}
