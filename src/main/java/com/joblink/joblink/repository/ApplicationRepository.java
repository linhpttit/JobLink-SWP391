package com.joblink.joblink.repository;

import com.joblink.joblink.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByJobIdAndSeekerId(Integer jobId, Integer seekerId);
}