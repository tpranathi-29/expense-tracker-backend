package com.example.expense_tracker.controller;

import com.example.expense_tracker.dto.*;
import com.example.expense_tracker.service.GroupSharingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
public class GroupController {

    private final GroupSharingService groupSharingService;

    public GroupController(GroupSharingService groupSharingService) {
        this.groupSharingService = groupSharingService;
    }

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(groupSharingService.createGroup(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> listGroups(Authentication authentication) {
        return ResponseEntity.ok(groupSharingService.listGroups(authentication.getName()));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupResponse> addMember(
            @PathVariable Long groupId,
            @Valid @RequestBody AddGroupMemberRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(groupSharingService.addMember(groupId, request, authentication.getName()));
    }

    @GetMapping("/{groupId}/expenses")
    public ResponseEntity<List<SharedExpenseResponse>> listSharedExpenses(
            @PathVariable Long groupId,
            Authentication authentication) {
        return ResponseEntity.ok(groupSharingService.listSharedExpenses(groupId, authentication.getName()));
    }

    @PostMapping("/{groupId}/expenses")
    public ResponseEntity<SharedExpenseResponse> createSharedExpense(
            @PathVariable Long groupId,
            @Valid @RequestBody SharedExpenseRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(groupSharingService.createSharedExpense(groupId, request, authentication.getName()));
    }

    @GetMapping("/{groupId}/balances")
    public ResponseEntity<List<BalanceResponse>> balances(
            @PathVariable Long groupId,
            Authentication authentication) {
        return ResponseEntity.ok(groupSharingService.balances(groupId, authentication.getName()));
    }

    @GetMapping("/{groupId}/settlements")
    public ResponseEntity<List<SettlementResponse>> settlements(
            @PathVariable Long groupId,
            Authentication authentication) {
        return ResponseEntity.ok(groupSharingService.settlements(groupId, authentication.getName()));
    }

    @PostMapping("/{groupId}/settlements")
    public ResponseEntity<SettlementResponse> settle(
            @PathVariable Long groupId,
            @Valid @RequestBody SettlementRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(groupSharingService.settle(groupId, request, authentication.getName()));
    }
}
