package com.joblink.joblink.employer.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.joblink.joblink.employer.application.model.JobPostingForm;
import com.joblink.joblink.entity.Category;
import com.joblink.joblink.entity.District;
import com.joblink.joblink.entity.Employer;
import com.joblink.joblink.entity.JobPosting;
import com.joblink.joblink.entity.Province;
import com.joblink.joblink.entity.Skill;
import com.joblink.joblink.repository.CategoryRepository;
import com.joblink.joblink.repository.DistrictRepository;
import com.joblink.joblink.repository.EmployerRepository;
import com.joblink.joblink.repository.JobPostingRepository;
import com.joblink.joblink.repository.ProvinceRepository;
import com.joblink.joblink.repository.SkillRepository;

import jakarta.transaction.Transactional;

@Service("NewJobPostingService")
public class JobPostingService implements IJobPostingService {
	private final JobPostingRepository jobPostingRepository;
	private final EmployerRepository employerRepository;
	private final CategoryRepository categoryRepository;
	private final SkillRepository skillRepository;
	private final ProvinceRepository provinceRepository;
	private final DistrictRepository districtRepository;

	public JobPostingService(JobPostingRepository jobPostingRepository, EmployerRepository employerRepository,
			CategoryRepository categoryRepository, SkillRepository skillRepository,
			ProvinceRepository provinceRepository, DistrictRepository districtRepository) {
		this.jobPostingRepository = jobPostingRepository;
		this.employerRepository = employerRepository;
		this.categoryRepository = categoryRepository;
		this.skillRepository = skillRepository;
		this.provinceRepository = provinceRepository;
		this.districtRepository = districtRepository;
	}

	@Override
	@Transactional
	public void create(JobPostingForm f) {
		if (f.getSalaryMin() != null && f.getSalaryMax() != null && f.getSalaryMin().compareTo(f.getSalaryMax()) > 0) {
			throw new IllegalArgumentException("Lương tối thiểu không được lớn hơn lương tối đa");
		}

		Employer employer = employerRepository.findById(f.getEmployerId())
				.orElseThrow(() -> new IllegalArgumentException("Employer không tồn tại"));

		Category category = (f.getCategoryId() == null) ? null
				: categoryRepository.findById(f.getCategoryId())
						.orElseThrow(() -> new IllegalArgumentException("Category không tồn tại"));

		Skill skill = skillRepository.findById(f.getSkillId())
				.orElseThrow(() -> new IllegalArgumentException("Skill không tồn tại"));

		Province province = (f.getProvinceId() == null) ? null
				: provinceRepository.findById(f.getProvinceId())
						.orElseThrow(() -> new IllegalArgumentException("Province không tồn tại"));

		District district = (f.getDistrictId() == null) ? null
				: districtRepository.findById(f.getDistrictId())
						.orElseThrow(() -> new IllegalArgumentException("District không tồn tại"));

		JobPosting jp = JobPosting.builder().status(f.getStatus()).title(f.getTitle()).position(f.getPosition())
				.streetAddress(f.getStreetAddress()).yearExperience(f.getYearExperience())
				.hiringNumber(f.getHiringNumber()).submissionDeadline(f.getSubmissionDeadline())
				.workType(f.getWorkType()).salaryMin(f.getSalaryMin()).salaryMax(f.getSalaryMax())
				.jobDescription(f.getJobDescription()).jobRequirements(f.getJobRequirements()).benefits(f.getBenefits())
				.contactName(f.getContactName()).contactEmail(f.getContactEmail()).contactPhone(f.getContactPhone())
				.postedAt(LocalDateTime.now()).employer(employer).category(category).skill(skill).province(province)
				.district(district).build();

		jobPostingRepository.save(jp);
	}

}
