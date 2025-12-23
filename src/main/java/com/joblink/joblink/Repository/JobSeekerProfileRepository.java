package com.joblink.joblink.Repository;

import com.joblink.joblink.entity.JobSeekerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobSeekerProfileRepository extends JpaRepository<JobSeekerProfile, Integer> {
    // Thêm phương thức tìm kiếm Profile ID bằng User ID
    Optional<JobSeekerProfile> findByUserId(Integer userId);
}