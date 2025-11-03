package com.joblink.joblink.employer.application.service;

import com.joblink.joblink.entity.Employer;

public interface IEmployerService {
	Employer getById(Long id);
    Long getCurrentEmployerId(); 
    Employer getCurrentEmployer();
}
