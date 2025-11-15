package com.joblink.joblink.Repository;

import com.joblink.joblink.entity.EmployerComplaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployerComplaintRepository extends JpaRepository<EmployerComplaint, Long> {
    List<EmployerComplaint> findByEmployerId(Long employerId);
    Optional<EmployerComplaint> findById(Long id);
    List<EmployerComplaint> findByJobSeekerSeekerId(int jobSeekerId);
    @Query("SELECT c FROM EmployerComplaint c " +
            "WHERE c.employer.id = :employerId " +  // chỉ lấy complaint của employer hiện tại
            "AND (:status IS NULL OR c.status = :status) " +
            "AND (:keyword IS NULL OR " +
            "LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.jobSeeker.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) ) " +
            "ORDER BY c.createdAt DESC")
    List<EmployerComplaint> searchByEmployer(@Param("employerId") Long employerId,
                                             @Param("status") String status,
                                             @Param("keyword") String keyword);
}
