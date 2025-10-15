package com.joblink.joblink.service;

import com.joblink.joblink.dto.JobPostingDto;
import com.joblink.joblink.entity.JobPosting;

import java.util.List;
import java.util.Optional;

public interface IJobPostingService {
    void createJobPosting(JobPostingDto dto);
    List<JobPosting> getAllJobPostings();
    Optional<JobPosting>  findJobPostingById(Long id);
}
