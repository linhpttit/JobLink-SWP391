// File: IJobPostingService.java (ĐÃ SỬA LẠI HOÀN CHỈNH)
package com.joblink.joblink.service;

import com.joblink.joblink.dto.JobPostingDto;
import com.joblink.joblink.entity.JobPosting; // Import đúng entity

import java.util.List;
import java.util.Optional;

public interface IJobPostingService {

    // Giữ nguyên: Nhận thêm employerId để không bị hard-code
    JobPosting createJobPosting(JobPostingDto dto, Long employerId);

    // Bổ sung phương thức cập nhật
    Optional<JobPosting> updateJobPosting(Long jobPostingId, JobPostingDto dto);

    void deleteJobPostingById(Long id);

    List<JobPosting> getAllJobPostings();

    // Sửa lại kiểu dữ liệu của ID thành Long cho nhất quán
    Optional<JobPosting> findJobPostingById(Long id);

    // Bổ sung phương thức tìm theo nhà tuyển dụng
    List<JobPosting> findJobPostingsByEmployer(Long employerId);

    // Bổ sung phương thức thay đổi trạng thái
    Optional<JobPosting> changeJobPostingStatus(Long id, String status);

    // ✅ BỔ SUNG PHƯƠNG THỨC CÒN THIẾU
    List<JobPosting> getRelatedJobs(Integer categoryId, Long excludeJobId);
}