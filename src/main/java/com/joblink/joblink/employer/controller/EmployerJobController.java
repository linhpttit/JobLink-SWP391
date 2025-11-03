package com.joblink.joblink.employer.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.joblink.joblink.employer.application.model.JobCardVM;
import com.joblink.joblink.employer.application.service.IJobService;
import com.joblink.joblink.employer.application.service.IProvinceService;
import com.joblink.joblink.entity.Province;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/employer")
public class EmployerJobController {

	private final IJobService jobService;
	private final IProvinceService provinceService;

	@GetMapping("/jobs")
	public String listJobs(@RequestParam(value = "q", required = false) String q,
			@RequestParam(value = "provinceId", required = false) Long provinceId,
			@RequestParam(value = "workType", required = false) String workType,
			@RequestParam(value = "minSalary", required = false) Integer minSalary,
			@RequestParam(value = "sort", defaultValue = "postedAt,DESC") String sort,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size, Model model) {
		// Build Sort safely
		String[] sp = sort.split(",");
		String sortProp = sp[0];
		Sort.Direction dir = (sp.length > 1 && "ASC".equalsIgnoreCase(sp[1])) ? Sort.Direction.ASC
				: Sort.Direction.DESC;

		Pageable pageable = PageRequest.of(page, size, Sort.by(new Sort.Order(dir, sortProp)));

		Page<JobCardVM> jobs = jobService.search(q, provinceId, workType, minSalary, pageable);

		List<Province> provinces = provinceService.findAll();

		model.addAttribute("jobs", jobs);
		model.addAttribute("provinces", provinces);

		// keep filter values
		model.addAttribute("q", q);
		model.addAttribute("provinceId", provinceId);
		model.addAttribute("workType", workType);
		model.addAttribute("minSalary", minSalary);
		model.addAttribute("sort", sort);

		return "employer/job-list";
	}
}
