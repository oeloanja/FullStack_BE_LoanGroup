package com.billit.loangroup_service.repository;

import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.enums.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface LoanGroupRepository extends JpaRepository<LoanGroup, Long> {
    List<LoanGroup> findAllByRiskLevelAndIsFulledFalseOrderByMemberCountDesc(RiskLevel riskLevel);
    List<LoanGroup> findByRiskLevelAndLoanGroupAccount_IsClosedFalse(RiskLevel riskLevel);
}
