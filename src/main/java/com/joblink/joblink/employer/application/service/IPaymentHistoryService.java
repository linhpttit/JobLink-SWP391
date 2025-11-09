package com.joblink.joblink.employer.application.service;

import org.springframework.data.domain.Page;

import com.joblink.joblink.repository.PaymentHistoryRepository;

public interface IPaymentHistoryService {
	public Page<PaymentHistoryRepository.Row> getHistory(Long userId, int page, int size);
}
