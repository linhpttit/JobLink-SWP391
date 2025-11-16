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

	@GetMapping("/findemployers/{employerId}")
	public String employerJobs(@PathVariable Long employerId, Model model) {
		try {
			// Lấy danh sách job còn active của employer - dùng query với JOIN FETCH để tránh LazyInitializationException
			List<JobPosting> jobs = jobPostingRepository.findByEmployerIdAndStatusWithEmployer(employerId, "ACTIVE");
			model.addAttribute("jobs", jobs);
			
			// Lấy tên công ty từ job đầu tiên nếu có
			if (!jobs.isEmpty() && jobs.get(0).getEmployer() != null) {
				model.addAttribute("companyName", jobs.get(0).getEmployer().getCompanyName());
			} else {
				model.addAttribute("companyName", "Nhà tuyển dụng");
			}
			
			return "employer-jobs";
		} catch (Exception e) {
			System.err.println("❌ Lỗi khi lấy jobs của employer " + employerId + ": " + e.getMessage());
			e.printStackTrace();
			model.addAttribute("jobs", java.util.Collections.emptyList());
			model.addAttribute("companyName", "Nhà tuyển dụng");
			return "employer-jobs";
		}
	}
	
	// Thêm endpoint để tương thích với route /employers/{employerId}
	@GetMapping("/employers/{employerId}")
	public String employerJobsAlternative(@PathVariable Long employerId, Model model) {
		return employerJobs(employerId, model);
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


