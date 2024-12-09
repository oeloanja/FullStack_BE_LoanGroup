package com.billit.loangroup_service.repository;

import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.enums.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface LoanGroupRepository extends JpaRepository<LoanGroup, Long> {
    @Query(value = """
        SELECT group_id
        FROM loan_group
        WHERE risk_level = CAST(:riskLevel AS SIGNED)
          AND is_fulled = false
          AND member_count < :maxMembers
        ORDER BY member_count DESC
        LIMIT 1
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    Optional<Long> findAvailableGroupId(@Param("riskLevel") int riskLevel, @Param("maxMembers") int maxMembers);

    @Modifying
    @Query(value = """
        UPDATE loan_group
        SET member_count = member_count + 1
        WHERE group_id = :groupId
        """, nativeQuery = true)
    int incrementMemberCount(@Param("groupId") Long groupId);

    @Query(value = """
            SELECT COUNT(*)
            FROM loan_group
            WHERE risk_level = CAST(:riskLevel AS SIGNED)
            AND member_count = 0
            AND is_fulled = false
            """,
            nativeQuery = true)
    int countByRiskLevelAndMemberCountAndIsFullFalse(@Param("riskLevel") int riskLevel);


    List<LoanGroup> findByRiskLevelAndLoanGroupAccount_IsClosedFalse(RiskLevel riskLevel);
}