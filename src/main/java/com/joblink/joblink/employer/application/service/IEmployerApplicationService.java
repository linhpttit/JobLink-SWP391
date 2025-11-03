package com.joblink.joblink.employer.application.service;

import com.joblink.joblink.employer.application.model.ApplicationsPageVM;

public interface IEmployerApplicationService {
	public ApplicationsPageVM getApplications(String q, String status, Long jobId, String date, int page, int size);
    String exportCsv(String q, String status, Long jobId, String date);
}
