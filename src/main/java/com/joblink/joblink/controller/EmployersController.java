package com.joblink.joblink.controller;

import com.joblink.joblink.Repository.JobPostingRepository;
import com.joblink.joblink.dao.EmployersSearchDao;
import com.joblink.joblink.entity.JobPosting;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class EmployersController {
	private final EmployersSearchDao employersSearchDao;
	private final JobPostingRepository jobPostingRepository;

	@GetMapping("/findemployers")
	public String employersPage() {
		return "find-empoyers";
	}

	@GetMapping("/employer/{employerId}")
	public String employerJobs(@PathVariable Long employerId, Model model) {
		// Lấy danh sách job còn active của employer
		List<JobPosting> all = jobPostingRepository.findAll();
		List<JobPosting> jobs = all.stream()
			.filter(j -> j.getEmployer() != null && j.getEmployer().getId() != null && j.getEmployer().getId().equals(employerId))
			.filter(j -> j.getStatus() != null && j.getStatus().equalsIgnoreCase("ACTIVE"))
			.toList();
		model.addAttribute("jobs", jobs);
		return "employer-jobs";
	}

	// API cho trang find employers
	@GetMapping("/api/employers/openposition")
	@ResponseBody
	public Map<String, Object> searchOpenEmployers(
		@RequestParam(required = false) String keyword,
		@RequestParam(required = false) String location,
		@RequestParam(required = false) String industry,
		@RequestParam(defaultValue = "most_jobs") String sort,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "12") int size
	) {
		return employersSearchDao.search(keyword, location, industry, sort, page, size);
	}
}


