package com.joblink.joblink.employer.application.service;

import java.time.LocalDate;

import com.joblink.joblink.employer.application.model.EmployerDashboardVM;

public interface IEmployerReportService {
	EmployerDashboardVM getDashboard(String range, LocalDate from, LocalDate to, Long jobId);
}
