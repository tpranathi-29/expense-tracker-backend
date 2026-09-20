package com.example.expense_tracker.service;

import com.example.expense_tracker.dto.*;
import com.example.expense_tracker.exception.ResourceNotFoundException;
import com.example.expense_tracker.model.*;
import com.example.expense_tracker.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GroupSharingService {

    private static final BigDecimal CENT = new BigDecimal("0.01");

    private final ExpenseGroupRepository groupRepository;
    private final SharedExpenseRepository sharedExpenseRepository;
    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;

    public GroupSharingService(
            ExpenseGroupRepository groupRepository,
            SharedExpenseRepository sharedExpenseRepository,
            SettlementRepository settlementRepository,
            UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.sharedExpenseRepository = sharedExpenseRepository;
        this.settlementRepository = settlementRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request, String ownerEmail) {
        User owner = user(ownerEmail);
        ExpenseGroup group = new ExpenseGroup();
        group.setName(request.name().trim());
        group.setOwner(owner);
        group.getMembers().add(owner);
        return toGroupResponse(groupRepository.save(group));
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> listGroups(String email) {
        return groupRepository.findDistinctByMembersEmailOrderByNameAsc(email).stream()
                .map(this::toGroupResponse)
                .toList();
    }

    @Transactional
    public GroupResponse addMember(Long groupId, AddGroupMemberRequest request, String requesterEmail) {
        ExpenseGroup group = memberGroup(groupId, requesterEmail);
        User member = user(request.email());
        group.getMembers().add(member);
        return toGroupResponse(groupRepository.save(group));
    }

    @Transactional(readOnly = true)
    public List<SharedExpenseResponse> listSharedExpenses(Long groupId, String email) {
        memberGroup(groupId, email);
        return sharedExpenseRepository.findByGroupIdOrderByDateDescIdDesc(groupId).stream()
                .map(this::toSharedExpenseResponse)
                .toList();
    }

    @Transactional
    public SharedExpenseResponse createSharedExpense(
            Long groupId,
            SharedExpenseRequest request,
            String requesterEmail) {
        ExpenseGroup group = memberGroup(groupId, requesterEmail);
        User payer = memberByEmail(group, request.payerEmail());

        Map<String, SharedExpenseRequest.ShareRequest> uniqueShares = request.shares().stream()
                .collect(Collectors.toMap(
                        share -> share.userEmail().toLowerCase(Locale.ROOT),
                        Function.identity(),
                        (first, second) -> {
                            throw new IllegalArgumentException("Each group member may appear only once in shares");
                        }));

        BigDecimal shareTotal = uniqueShares.values().stream()
                .map(SharedExpenseRequest.ShareRequest::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (shareTotal.subtract(request.totalAmount()).abs().compareTo(CENT) > 0) {
            throw new IllegalArgumentException("Share amounts must add up exactly to the total amount");
        }

        SharedExpense expense = new SharedExpense();
        expense.setDescription(request.description().trim());
        expense.setTotalAmount(request.totalAmount().setScale(2));
        expense.setDate(request.date());
        expense.setGroup(group);
        expense.setPayer(payer);

        for (SharedExpenseRequest.ShareRequest shareRequest : uniqueShares.values()) {
            ExpenseShare share = new ExpenseShare();
            share.setSharedExpense(expense);
            share.setUser(memberByEmail(group, shareRequest.userEmail()));
            share.setAmount(shareRequest.amount().setScale(2));
            expense.getShares().add(share);
        }

        return toSharedExpenseResponse(sharedExpenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public List<BalanceResponse> balances(Long groupId, String email) {
        ExpenseGroup group = memberGroup(groupId, email);
        return calculateBalances(group).values().stream()
                .filter(value -> value.amount().compareTo(CENT) >= 0)
                .toList();
    }

    @Transactional
    public SettlementResponse settle(Long groupId, SettlementRequest request, String fromEmail) {
        ExpenseGroup group = memberGroup(groupId, fromEmail);
        User from = memberByEmail(group, fromEmail);
        User to = memberByEmail(group, request.toUserEmail());
        if (from.getId().equals(to.getId())) {
            throw new IllegalArgumentException("A user cannot settle with themselves");
        }

        BigDecimal owed = calculateBalances(group).values().stream()
                .filter(balance -> balance.debtorEmail().equalsIgnoreCase(from.getEmail()))
                .filter(balance -> balance.creditorEmail().equalsIgnoreCase(to.getEmail()))
                .map(BalanceResponse::amount)
                .findFirst()
                .orElse(BigDecimal.ZERO);
        if (request.amount().compareTo(owed) > 0) {
            throw new IllegalArgumentException("Settlement cannot be greater than the current amount owed");
        }

        Settlement settlement = new Settlement();
        settlement.setGroup(group);
        settlement.setFromUser(from);
        settlement.setToUser(to);
        settlement.setAmount(request.amount().setScale(2));
        settlement.setNote(request.note());
        settlement.setCreatedAt(LocalDateTime.now());
        return toSettlementResponse(settlementRepository.save(settlement));
    }

    @Transactional(readOnly = true)
    public List<SettlementResponse> settlements(Long groupId, String email) {
        memberGroup(groupId, email);
        return settlementRepository.findByGroupIdOrderByCreatedAtDesc(groupId).stream()
                .map(this::toSettlementResponse)
                .toList();
    }

    private Map<Pair, BalanceResponse> calculateBalances(ExpenseGroup group) {
        Map<Pair, BigDecimal> net = new HashMap<>();
        for (SharedExpense expense : sharedExpenseRepository.findByGroupIdOrderByDateDescIdDesc(group.getId())) {
            for (ExpenseShare share : expense.getShares()) {
                if (!share.getUser().getId().equals(expense.getPayer().getId())) {
                    addDirected(net, share.getUser(), expense.getPayer(), share.getAmount());
                }
            }
        }
        for (Settlement settlement : settlementRepository.findByGroupIdOrderByCreatedAtDesc(group.getId())) {
            addDirected(net, settlement.getFromUser(), settlement.getToUser(), settlement.getAmount().negate());
        }

        Map<Pair, BalanceResponse> result = new HashMap<>();
        for (Map.Entry<Pair, BigDecimal> entry : net.entrySet()) {
            BigDecimal value = entry.getValue().setScale(2);
            if (value.signum() == 0) {
                continue;
            }
            User first = entry.getKey().first;
            User second = entry.getKey().second;
            User debtor = value.signum() > 0 ? first : second;
            User creditor = value.signum() > 0 ? second : first;
            result.put(entry.getKey(), new BalanceResponse(
                    debtor.getEmail(), debtor.getName(),
                    creditor.getEmail(), creditor.getName(), value.abs()));
        }
        return result;
    }

    private void addDirected(Map<Pair, BigDecimal> net, User debtor, User creditor, BigDecimal amount) {
        if (amount.signum() == 0 || debtor.getId().equals(creditor.getId())) {
            return;
        }
        Pair pair = debtor.getId() < creditor.getId()
                ? new Pair(debtor, creditor)
                : new Pair(creditor, debtor);
        BigDecimal signedAmount = debtor.getId().equals(pair.first.getId()) ? amount : amount.negate();
        net.merge(pair, signedAmount, BigDecimal::add);
    }

    private ExpenseGroup memberGroup(Long id, String email) {
        ExpenseGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        if (group.getMembers().stream().noneMatch(member -> member.getEmail().equalsIgnoreCase(email))) {
            throw new ResourceNotFoundException("You are not a member of this group");
        }
        return group;
    }

    private User memberByEmail(ExpenseGroup group, String email) {
        return group.getMembers().stream()
                .filter(member -> member.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this group"));
    }

    private User user(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private GroupResponse toGroupResponse(ExpenseGroup group) {
        return new GroupResponse(group.getId(), group.getName(), group.getOwner().getEmail(),
                group.getMembers().stream()
                        .sorted(Comparator.comparing(User::getName))
                        .map(member -> new GroupResponse.MemberResponse(member.getId(), member.getName(), member.getEmail()))
                        .toList());
    }

    private SharedExpenseResponse toSharedExpenseResponse(SharedExpense expense) {
        return new SharedExpenseResponse(expense.getId(), expense.getDescription(), expense.getTotalAmount(), expense.getDate(),
                member(expense.getPayer()), expense.getShares().stream()
                        .map(share -> new SharedExpenseResponse.Share(member(share.getUser()), share.getAmount()))
                        .toList());
    }

    private SharedExpenseResponse.Member member(User user) {
        return new SharedExpenseResponse.Member(user.getId(), user.getName(), user.getEmail());
    }

    private SettlementResponse toSettlementResponse(Settlement settlement) {
        return new SettlementResponse(settlement.getId(), settlement.getFromUser().getEmail(), settlement.getToUser().getEmail(),
                settlement.getAmount(), settlement.getNote(), settlement.getCreatedAt());
    }

    private record Pair(User first, User second) {
    }
}
