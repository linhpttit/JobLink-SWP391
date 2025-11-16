// File: JobPostingService.java (ĐÃ SỬA LỖI HOÀN CHỈNH)
package com.joblink.joblink.service;

import com.joblink.joblink.dto.JobPostingDto;
import com.joblink.joblink.entity.*;
import com.joblink.joblink.Repository.*; // Gộp các import repository
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobPostingService implements IJobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final com.joblink.joblink.Repository.SkillRepository skillRepository;
    private final com.joblink.joblink.Repository.ProvinceRepository provinceRepository;
    private final com.joblink.joblink.Repository.DistrictRepository districtRepository;
    private final EmployerRepository employerRepository;

    @Override
    @Transactional
    public JobPosting createJobPosting(JobPostingDto dto, Integer employerId) {
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

        Employer employer = employerRepository.getById(Long.valueOf(employerId));
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
    public List<JobPosting> getJobPostingsByEmployerId(Integer employerId) {
        return  jobPostingRepository.findByEmployerId(Long.valueOf(employerId));
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
    public void hideJob(Long jobId) {
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        job.setStatus("Deactive");
        jobPostingRepository.save(job);
    }

    @Transactional
    @Override
    public JobPosting toggleJobStatus(Long jobId) {
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job không tồn tại với id: " + jobId));

        if ("active".equalsIgnoreCase(job.getStatus())) {
            job.setStatus("inactive"); // Tạm ẩn
        } else {
            job.setStatus("active");   // Kích hoạt
        }

        return jobPostingRepository.save(job);
    }
    @Override
    public List<JobPosting> filterJobs(String keyword, String status, String skill, String date) {
        List<JobPosting> jobs = getAllJobPostings(); // lấy tất cả

        if(keyword != null && !keyword.isEmpty()) {
            jobs = jobs.stream()
                    .filter(j -> j.getTitle().toLowerCase().contains(keyword.toLowerCase())
                            || j.getEmployer().getCompanyName().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if(status != null && !status.isEmpty()) {
            jobs = jobs.stream()
                    .filter(j -> status.equals(j.getStatus()))
                    .collect(Collectors.toList());
        }

        if(skill != null && !skill.isEmpty()) {
            jobs = jobs.stream()
                    .filter(j -> skill.equals(j.getSkill().getName())) // code tương ứng với select option
                    .collect(Collectors.toList());
        }

        if(date != null && !date.isEmpty()) {
            LocalDate now = LocalDate.now();
            jobs = jobs.stream().filter(j -> {
                LocalDate posted = j.getPostedAt().toLocalDate();
                switch(date) {
                    case "today": return posted.isEqual(now);
                    case "week": return posted.isAfter(now.minusDays(7)) || posted.isEqual(now.minusDays(7));
                    case "month": return posted.isAfter(now.minusMonths(1)) || posted.isEqual(now.minusMonths(1));
                    default: return true;
                }
            }).collect(Collectors.toList());
        }

        return jobs;
    }
}