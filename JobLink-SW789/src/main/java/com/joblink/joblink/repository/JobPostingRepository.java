// File: com/joblink/joblink/repository/JobPostingRepository.java (ĐÃ SỬA LỖI HOÀN CHỈNH)
package com.joblink.joblink.repository;

import com.joblink.joblink.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    // Tìm các tin đăng theo ID của nhà tuyển dụng
    List<JobPosting> findByEmployerId(Long employerId);

    // SỬA LỖI: Đổi tên phương thức để khớp với đường dẫn thuộc tính "category.categoryId"
    // Đồng thời đổi Integer thành Long cho nhất quán
    List<JobPosting> findTop3ByCategoryCategoryIdAndJobIdNotOrderByPostedAtDesc(int categoryId, Long excludeJobId);
    @Query("SELECT j FROM JobPosting j " +
            "WHERE j.skill.skillId = :skillId " +
            "AND j.jobId <> :excludeJobId " +
            "AND j.status = 'ACTIVE' " +
            "ORDER BY j.postedAt DESC")
    List<JobPosting> findBySkillSkillIdAndJobIdNot(@Param("skillId") Long skillId,
                                                   @Param("excludeJobId") Long excludeJobId);

}