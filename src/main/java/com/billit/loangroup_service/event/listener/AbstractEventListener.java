package com.billit.loangroup_service.event.listener;

import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.repository.LoanGroupRepository;
import com.billit.loangroup_service.service.LoanGroupAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractEventListener<T> {
    protected final LoanGroupAccountService loanGroupAccountService;
    protected final LoanGroupRepository loanGroupRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public void handleEvent(T event) {
        log.debug("Handling event for type: {}", event.getClass().getSimpleName());
        LoanGroup group = loanGroupRepository.findById(getGroupId(event))
                .orElseThrow(() -> new IllegalStateException("Group not found"));
        processEventWithoutTransaction(group, event);
    }

    protected abstract Long getGroupId(T event);

    protected void processEventWithoutTransaction(LoanGroup group, T event) {
        processEvent(group, event);
    }

    protected abstract void processEvent(LoanGroup group, T event);
}
