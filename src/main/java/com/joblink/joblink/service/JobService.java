package com.joblink.joblink.service;

import com.joblink.joblink.auth.model.JobSearchResult;
import com.joblink.joblink.dao.JobDAO;
import com.joblink.joblink.entity.JobPosting;
import com.joblink.joblink.entity.Application;
import com.joblink.joblink.repository.JobPostingRepository;
import com.joblink.joblink.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobDAO jobDAO;
    private final JobPostingRepository jobPostingRepository;
    private final ApplicationRepository applicationRepository;

    public List<JobSearchResult> searchJobs(String skills, String location,
                                            Double minSalary, Double maxSalary,
                                            int page, int size) {
        return jobDAO.searchJobs(skills, location, minSalary, maxSalary, page, size);
    }

    public Optional<JobPosting> getJobById(Long jobId) {
        return jobPostingRepository.findById(jobId);
    }

    public List<JobPosting> getRelatedJobs(Integer categoryId, Long excludeJobId) {
        if (categoryId == null) {
            return Collections.emptyList();
        }
        return jobPostingRepository.findTop3ByCategoryCategoryIdAndJobIdNotOrderByPostedAtDesc(categoryId, excludeJobId);
    }

    public List<JobPosting> getRelatedJobsBySkill(Long skillId, Long excludeJobId) {
        return jobPostingRepository.findBySkillSkillIdAndJobIdNot(skillId, excludeJobId);
    }

    /**
     * Kiểm tra xem seeker đã ứng tuyển vào job này chưa
     */
    public boolean hasApplied(int jobId, int seekerId) {
        return applicationRepository.existsByJobIdAndSeekerId(jobId, seekerId);
    }

    /**
     * Tạo application mới (ứng tuyển)
     */
    @Transactional
    public void applyJob(int jobId, int seekerId, String cvUrl, String coverLetter) {
        if (hasApplied(jobId, seekerId)) {
            throw new IllegalStateException("Bạn đã ứng tuyển công việc này rồi");
        }

        Application application = new Application();
        application.setJobId(jobId);
        application.setSeekerId(seekerId);
        application.setCvUrl(cvUrl);
        application.setNote(coverLetter);
        application.setStatus("submitted");
        application.setAppliedAt(LocalDateTime.now());
        application.setLastStatusAt(LocalDateTime.now());

        String statusLog = String.format(
                "[{\"ts\":\"%s\",\"action\":\"applied\",\"new\":\"submitted\"}]",
                LocalDateTime.now().toString()
        );
        application.setStatusLog(statusLog);

        applicationRepository.save(application);
    }
}