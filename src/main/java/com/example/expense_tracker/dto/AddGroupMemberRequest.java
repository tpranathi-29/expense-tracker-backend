package com.example.expense_tracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AddGroupMemberRequest(@NotBlank @Email String email) {
}
