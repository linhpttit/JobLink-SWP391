// File: JobPostingService.java (ĐÃ SỬA LỖI HOÀN CHỈNH)
package com.joblink.joblink.service;

import com.joblink.joblink.dto.JobPostingDto;
import com.joblink.joblink.entity.*;
import com.joblink.joblink.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobPostingService implements IJobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final com.joblink.joblink.repository.SkillRepository skillRepository;
    private final com.joblink.joblink.repository.ProvinceRepository provinceRepository;
    private final com.joblink.joblink.repository.DistrictRepository districtRepository;
    private final EmployerRepository employerRepository;

    @Override
    @Transactional
    public JobPosting createJobPosting(JobPostingDto dto) {
        JobPosting posting = new JobPosting();

        posting.setTitle(dto.getTitle());
        Skill skill = skillRepository.findById(dto.getSkillId())
                .orElseThrow(() -> new RuntimeException("Skill not found"));
        posting.setSkill(skill);
        posting.setYearExperience(dto.getYearExperience());
        posting.setHiringNumber(dto.getHiringNumber());
        posting.setSubmissionDeadline(dto.getSubmissionDeadline());
        Province province = provinceRepository.findById(dto.getProvinceId())
                .orElseThrow(() -> new RuntimeException("Province not found"));
        posting.setProvince(province);
        District district = districtRepository.findById(dto.getDistrictId())
                .orElseThrow(() -> new RuntimeException("District not found"));
        posting.setDistrict(district);
        posting.setStreetAddress(dto.getStreetAddress());
        posting.setWorkType(dto.getWorkType());
        posting.setPosition(dto.getPosition());
        posting.setSalaryMin(dto.getSalaryMin());
        posting.setSalaryMax(dto.getSalaryMax());
        posting.setJobDescription(dto.getJobDescription());
        posting.setJobRequirements(dto.getJobRequirements());
        posting.setBenefits(dto.getBenefits());
        posting.setContactName(dto.getContactName());
        posting.setContactEmail(dto.getContactEmail());
        posting.setContactPhone(dto.getContactPhone());

        Employer employer = new Employer();
        employer.setId(1L);
        posting.setEmployer(employer);

        jobPostingRepository.save(posting);
        return posting;
    }

    @Override
    @Transactional
    public Optional<JobPosting> updateJobPosting(Long jobPostingId, JobPostingDto dto) {
        return jobPostingRepository.findById(jobPostingId).map(posting -> {
            Skill skill = skillRepository.findById(dto.getSkillId())
                    .orElseThrow(() -> new RuntimeException("Skill not found"));
            Province province = provinceRepository.findById(dto.getProvinceId())
                    .orElseThrow(() -> new RuntimeException("Province not found"));
            District district = districtRepository.findById(dto.getDistrictId())
                    .orElseThrow(() -> new RuntimeException("District not found"));

            // BỔ SUNG CÁC TRƯỜNG CÒN THIẾU
            posting.setTitle(dto.getTitle());
            posting.setYearExperience(dto.getYearExperience());
            posting.setHiringNumber(dto.getHiringNumber());
            posting.setSubmissionDeadline(dto.getSubmissionDeadline());
            posting.setStreetAddress(dto.getStreetAddress());
            posting.setWorkType(dto.getWorkType());
            posting.setPosition(dto.getPosition());
            posting.setSalaryMin(dto.getSalaryMin());
            posting.setSalaryMax(dto.getSalaryMax());
            posting.setJobDescription(dto.getJobDescription());
            posting.setJobRequirements(dto.getJobRequirements());
            posting.setBenefits(dto.getBenefits());
            posting.setContactName(dto.getContactName());
            posting.setContactEmail(dto.getContactEmail());
            posting.setContactPhone(dto.getContactPhone());

            posting.setSkill(skill);
            posting.setProvince(province);
            posting.setDistrict(district);

            return jobPostingRepository.save(posting);
        });
    }

    @Override
    public void deleteJobPostingById(Long id) {
        jobPostingRepository.deleteById(id);
    }

    @Override
    public void editJobPostingByEntity(Long id, JobPosting updatedPosting) {
        JobPosting posting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        posting.setTitle(updatedPosting.getTitle());
        posting.setSkill(updatedPosting.getSkill());
        posting.setProvince(updatedPosting.getProvince());
        posting.setDistrict(updatedPosting.getDistrict());
        posting.setSalaryMax(updatedPosting.getSalaryMax());
        posting.setJobDescription(updatedPosting.getJobDescription());
        posting.setJobRequirements(updatedPosting.getJobRequirements());
        posting.setBenefits(updatedPosting.getBenefits());
        posting.setContactName(updatedPosting.getContactName());
        posting.setContactEmail(updatedPosting.getContactEmail());
        posting.setContactPhone(updatedPosting.getContactPhone());
        jobPostingRepository.save(posting);

    }


    @Override
    public List<JobPosting> getAllJobPostings() {
        return jobPostingRepository.findAll();
    }

    @Override
    public Optional<JobPosting> findJobPostingById(Long id) {
        return jobPostingRepository.findById(id);
    }

    @Override
    public List<JobPosting> findJobPostingsByEmployer(Long employerId) {
        // Đảm bảo bạn đã thêm phương thức này vào JobPostingRepository
        return jobPostingRepository.findByEmployerId(employerId);
    }

    @Override
    public Optional<JobPosting> changeJobPostingStatus(Long id, String status) {
        return jobPostingRepository.findById(id).map(posting -> {
            posting.setStatus(status);
            return jobPostingRepository.save(posting);
        });
    }

    @Override
    public List<JobPosting> getRelatedJobs(Integer categoryId, Long excludeJobId) {
        return List.of();
    }

    @Override
    public long countOpenJobPosting() {
        return jobPostingRepository.countByStatus("ACTIVE");
    }

    // Bạn cần thêm phương thức getRelatedJobs vào đây để implement interface
}