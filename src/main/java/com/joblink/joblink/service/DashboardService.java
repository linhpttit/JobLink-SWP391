package com.joblink.joblink.service;

import com.joblink.joblink.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobPostingRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public long countEmployers() {
        return userRepository.countEmployers();
    }

    public long countJobSeeker() {
        return userRepository.countJobSeekers();
    }

    public long countJobPosts() {
        return jobRepository.count();
    }

    public long countApplications() {
        return applicationRepository.count();
    }

    public double getTotalRevenue() {
        return paymentRepository.getTotalRevenue();
    }

}
