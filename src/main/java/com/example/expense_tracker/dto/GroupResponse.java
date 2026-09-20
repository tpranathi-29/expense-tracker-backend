package com.example.expense_tracker.dto;

import java.util.List;

public record GroupResponse(Long id, String name, String ownerEmail, List<MemberResponse> members) {
    public record MemberResponse(Long id, String name, String email) {
    }
}
