package com.billit.loangroup_service.event.listener;

import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.event.domain.LoanGroupFullEvent;
import com.billit.loangroup_service.repository.LoanGroupRepository;
import com.billit.loangroup_service.service.LoanGroupAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class LoanGroupFullEventListener extends AbstractEventListener<LoanGroupFullEvent> {

    public LoanGroupFullEventListener(LoanGroupAccountService loanGroupAccountService, LoanGroupRepository loanGroupRepository) {
        super(loanGroupAccountService, loanGroupRepository);
    }

    @Override
    protected Long getGroupId(LoanGroupFullEvent event) {
        return Long.valueOf(event.getGroupId());
    }

    @Override
    protected void processEvent(LoanGroup group, LoanGroupFullEvent event) {
        log.info("Processing LoanGroupFullEvent for groupId: {}", group.getGroupId());
        try {
            loanGroupAccountService.createLoanGroupAccount(group);
            log.info("LoanGroupAccount creation completed for groupId: {}", group.getGroupId());
        } catch (Exception e) {
            log.error("Error while creating LoanGroupAccount for groupId: {}", group.getGroupId(), e);
            throw e;
        }
    }

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onApplicationEvent(LoanGroupFullEvent event) {
        handleEvent(event);
    }
}