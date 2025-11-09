package com.joblink.joblink.employer.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.joblink.joblink.repository.PaymentHistoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentHistoryService implements IPaymentHistoryService{

    private final PaymentHistoryRepository repo;

    @Override
    @Transactional
    public Page<PaymentHistoryRepository.Row> getHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "paidAt"));
        return repo.findHistoryByUserId(userId, pageable);
    }
}
