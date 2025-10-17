package com.joblink.joblink.repository;

import com.joblink.joblink.entity.Employer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployerReposistory extends JpaRepository<Employer, Long> {
}