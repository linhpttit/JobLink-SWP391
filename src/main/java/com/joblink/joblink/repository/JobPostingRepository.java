// File: com/joblink/joblink/repository/JobPostingRepository.java (ĐÃ SỬA LỖI HOÀN CHỈNH)
package com.joblink.joblink.Repository;

import com.joblink.joblink.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    // Tìm các tin đăng theo ID của nhà tuyển dụng
    List<JobPosting> findByEmployerId(Long employerId);

    // SỬA LỖI: Đổi tên phương thức để khớp với đường dẫn thuộc tính "category.categoryId"
    // Đồng thời đổi Integer thành Long cho nhất quán
    List<JobPosting> findTop3ByCategoryCategoryIdAndJobIdNotOrderByPostedAtDesc(int categoryId, Long excludeJobId);
}