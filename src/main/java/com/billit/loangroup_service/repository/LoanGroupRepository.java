package com.billit.loangroup_service.repository;

import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.enums.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanGroupRepository extends JpaRepository<LoanGroup, Long> {
    List<LoanGroup> findByRiskLevelAndIsFulledTrue(RiskLevel riskLevel);
    LoanGroup findByRiskLevelAndIsFulledFalse(RiskLevel riskLevel);
}
