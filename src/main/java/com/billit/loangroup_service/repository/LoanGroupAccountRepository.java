package com.billit.loangroup_service.repository;

import com.billit.loangroup_service.entity.LoanGroupAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanGroupAccountRepository extends JpaRepository<LoanGroupAccount, Integer> {
    Optional<LoanGroupAccount> findByGroup_GroupId(Integer groupId);
}
