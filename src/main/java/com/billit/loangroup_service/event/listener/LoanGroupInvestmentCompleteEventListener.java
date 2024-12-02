//package com.billit.loangroup_service.event.listener;
//
//import com.billit.loangroup_service.entity.LoanGroup;
//import com.billit.loangroup_service.event.domain.LoanGroupInvestmentCompleteEvent;
//import com.billit.loangroup_service.repository.LoanGroupRepository;
//import com.billit.loangroup_service.service.LoanGroupAccountService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.transaction.event.TransactionPhase;
//import org.springframework.transaction.event.TransactionalEventListener;
//
//import java.math.BigDecimal;
//
//@Slf4j
//@Component
//public class LoanGroupInvestmentCompleteEventListener extends AbstractEventListener<LoanGroupInvestmentCompleteEvent> {
//
//    public LoanGroupInvestmentCompleteEventListener(LoanGroupAccountService loanGroupAccountService, LoanGroupRepository loanGroupRepository) {
//        super(loanGroupAccountService, loanGroupRepository);
//    }
//
//    @Override
//    protected Long getGroupId(LoanGroupInvestmentCompleteEvent event) {
//        return Long.valueOf(event.getGroupId());
//    }
//
//    @Override
//    protected void processEvent(LoanGroup group, LoanGroupInvestmentCompleteEvent event) {
//        log.info("Processing LoanGroupInvestmentCompleteEvent for groupId: {}", group.getGroupId());
//        try {
//            BigDecimal excess = event.getCurrentBalance().subtract(event.getRequiredAmount());
//            loanGroupAccountService.processDisbursement(group, excess);
//            log.info("Disbursement processed for groupId: {}", group.getGroupId());
//        } catch (Exception e) {
//            log.error("Error while processing disbursement for groupId: {}", group.getGroupId(), e);
//        }
//    }
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void onApplicationEvent(LoanGroupInvestmentCompleteEvent event) {
//        handleEvent(event);
//    }
//}
