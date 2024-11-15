package com.billit.loangroup_service.repository;

import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.entity.PlatformAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformAccountRepository extends JpaRepository<PlatformAccount, Integer> {
    Optional<PlatformAccount> findByGroup(LoanGroup group);
}
