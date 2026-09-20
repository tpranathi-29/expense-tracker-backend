package com.example.expense_tracker.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateGroupRequest(@NotBlank String name) {
}
