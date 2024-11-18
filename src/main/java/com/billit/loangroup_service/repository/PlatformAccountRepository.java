package com.billit.loangroup_service.repository;

import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.entity.LoanGroupAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlatformAccountRepository extends JpaRepository<LoanGroupAccount, Integer> {
    Optional<LoanGroupAccount> findByGroup(LoanGroup group);
    List<LoanGroupAccount> findByIsClosedFalse();

}
