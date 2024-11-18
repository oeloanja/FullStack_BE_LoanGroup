package com.billit.loangroup_service.event.listener;

import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.event.domain.LoanGroupFullEvent;
import com.billit.loangroup_service.repository.LoanGroupRepository;
import com.billit.loangroup_service.service.LoanGroupAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LoanGroupFullEventListener {

    private final LoanGroupAccountService loanGroupAccountService;
    private final LoanGroupRepository loanGroupRepository;

    @EventListener
    @Async
    public void handleLoanGroupFullEvent(LoanGroupFullEvent event) {
        // 트랜잭션 없이 비동기로 실행
        createPlatformAccountWithNewTransaction(String.valueOf(event.getGroupId()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createPlatformAccountWithNewTransaction(String groupId) {
        LoanGroup group = loanGroupRepository.findById(Long.valueOf(groupId))
                .orElseThrow(() -> new IllegalStateException("Group not found"));
        loanGroupAccountService.createPlatformAccount(group);
    }
}
