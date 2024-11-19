package com.billit.loangroup_service.service;

import com.billit.loangroup_service.cache.LoanGroupAccountCache;
import com.billit.loangroup_service.connection.client.LoanServiceClient;
import com.billit.loangroup_service.connection.dto.LoanResponseClientDto;
import com.billit.loangroup_service.dto.LoanGroupAccountResponseDto;
import com.billit.loangroup_service.entity.LoanGroup;
import com.billit.loangroup_service.entity.LoanGroupAccount;
import com.billit.loangroup_service.repository.LoanGroupRepository;
import com.billit.loangroup_service.repository.LoanGroupAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoanGroupAccountService {
    private final LoanGroupAccountRepository loanGroupAccountRepository;
    private final LoanGroupAccountCache loanGroupAccountCache;
    private final LoanServiceClient loanServiceClient;
    private final LoanGroupRepository loanGroupRepository;
    private final RedisTemplate<String, LoanGroupAccount> redisTemplate;

    // 계좌 생성
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createPlatformAccount(LoanGroup group) {
        LoanGroup managedGroup = loanGroupRepository.findById(Long.valueOf(group.getGroupId()))
                .orElseThrow(() -> new IllegalStateException("Group not found"));

        // Loan 서비스에서 해당 그룹의 대출 목록 조회
        List<LoanResponseClientDto> groupLoans = loanServiceClient.getLoansByGroupId(group.getGroupId());

        // 총 대출금액 계산
        BigDecimal totalLoanAmount = groupLoans.stream()
                .map(LoanResponseClientDto::getLoanAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);  // add 메서드 명확히 지정

        BigDecimal averageIntRate = calculateIntRateAvg(groupLoans);
        managedGroup.updateIntRateAvg(averageIntRate);
        loanGroupRepository.save(managedGroup);  // 업데이트된 평균 이자율 저장


        LoanGroupAccount account = new LoanGroupAccount(
                managedGroup,
                totalLoanAmount,
                BigDecimal.ZERO,
                LocalDateTime.now()
        );
        loanGroupAccountRepository.save(account);
        loanGroupAccountCache.saveToCache(account);
    }

    // 현재 입금액 수정: LoanGroupAccount entity 데이터가 편집됨
    @Transactional
    public void updateLoanGroupAccountBalance(Integer loanGroupId, BigDecimal amount) {
        LoanGroupAccount target = loanGroupAccountRepository.findByGroup_GroupId(loanGroupId);
        target.updateBalance(amount);
        loanGroupAccountCache.updateBalanceInCache(target.getLoanGroupAccountId(), amount);

        if(target.getCurrentBalance().compareTo(target.getRequiredAmount()) >= 0 ){
            target.closeAccount();
        }
    }

    // GroupId로 Account 찾기
    public LoanGroupAccountResponseDto getAccount(Integer groupId) {
        LoanGroupAccount account = loanGroupAccountRepository.findByGroup_GroupId(groupId);
        return LoanGroupAccountResponseDto.from(account);
    }

    // 이자율 평균 계산
    public BigDecimal calculateIntRateAvg(List<LoanResponseClientDto> groupLoans){
        return groupLoans.stream()
                .map(LoanResponseClientDto::getIntRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(groupLoans.size()), 2, RoundingMode.HALF_UP);
    }
}
