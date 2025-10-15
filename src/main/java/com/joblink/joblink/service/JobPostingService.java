package com.joblink.joblink.service;

import com.joblink.joblink.dto.JobPostingDto;
import com.joblink.joblink.entity.*;
import com.joblink.joblink.repository.DistrictRepository;
import com.joblink.joblink.repository.JobPostingRepository;
import com.joblink.joblink.repository.ProvinceRepository;
import com.joblink.joblink.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobPostingService implements  IJobPostingService{
    private final JobPostingRepository jobPostingRepository;
    private final SkillRepository skillRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;

    @Override
    public void createJobPosting(JobPostingDto dto) {
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
    }

    @Override
    public List<JobPosting> getAllJobPostings() {
        return jobPostingRepository.findAll();
    }

    @Override
    public Optional<JobPosting> findJobPostingById(Long id) {
        return  jobPostingRepository.findById(id);
    }
}
