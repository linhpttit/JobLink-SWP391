// File: com/joblink/joblink/service/JobService.java (ĐÃ VIẾT LẠI HOÀN CHỈNH)
package com.joblink.joblink.service;

import com.joblink.joblink.auth.model.JobSearchResult; // Dùng cho search
import com.joblink.joblink.dao.JobDAO;
import com.joblink.joblink.dto.JobPostingDto;
import com.joblink.joblink.entity.JobPosting; // Import đúng entity
import com.joblink.joblink.Repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor // Sử dụng constructor injection cho tất cả
public class JobService {

    private final JobDAO jobDAO; // Giữ lại DAO chỉ cho việc search
    private final JobPostingRepository jobPostingRepository; // Dùng repository cho các thao tác CRUD

    // 1. Phương thức tìm kiếm (giữ nguyên, gọi DAO)
    public List<JobSearchResult> searchJobs(String skills, String location,
                                            Double minSalary, Double maxSalary,
                                            int page, int size) {
        return jobDAO.searchJobs(skills, location, minSalary, maxSalary, page, size);
    }

    // 2. Lấy Job theo ID (dùng repository)
    public Optional<JobPosting> getJobById(Long jobId) {
        return jobPostingRepository.findById(jobId);
    }

    // 3. Lấy các Job liên quan (dùng repository)
    public List<JobPosting> getRelatedJobs(Integer categoryId, Long excludeJobId) {
        if (categoryId == null) {
            return Collections.emptyList(); // Trả về danh sách rỗng an toàn
        }
        return jobPostingRepository.findTop3ByCategoryCategoryIdAndJobIdNotOrderByPostedAtDesc(categoryId, excludeJobId);
    }

    // (Bạn có thể thêm các phương thức khác từ IJobPostingService vào đây nếu cần)
}