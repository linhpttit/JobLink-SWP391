package com.joblink.joblink.repository;

import com.joblink.joblink.entity.Employer;
import com.joblink.joblink.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
}
