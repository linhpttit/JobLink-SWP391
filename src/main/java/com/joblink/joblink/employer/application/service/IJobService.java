package com.joblink.joblink.employer.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.joblink.joblink.employer.application.model.JobCardVM;

public interface IJobService {
	 public Page<JobCardVM> search(String q, Long provinceId, String workType, Integer minSalary,
             Pageable pageable);
}
