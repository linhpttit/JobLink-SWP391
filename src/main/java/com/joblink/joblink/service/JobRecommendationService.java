package com.joblink.joblink.service;

import com.joblink.joblink.Repository.JobPostingRepository;
import com.joblink.joblink.Repository.SkillRepository;
import com.joblink.joblink.dto.CVEvaluationResult;
import com.joblink.joblink.entity.JobPosting;
import com.joblink.joblink.entity.Skill;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobRecommendationService {
	private final JobPostingRepository jobPostingRepository;
	private final SkillRepository skillRepository;

	public List<JobPosting> recommendJobs(CVEvaluationResult eval, int limit) {
		List<String> skills = eval.getExtractedSkills() == null ? List.of() : eval.getExtractedSkills();
		if (skills.isEmpty()) {
			// fallback: trả về các job mới nhất
			return jobPostingRepository.findAll()
				.stream()
				.sorted(Comparator.comparing(JobPosting::getPostedAt,
						Comparator.nullsLast(Comparator.naturalOrder())).reversed())
				.limit(limit)
				.collect(Collectors.toList());
		}
		// map tên kỹ năng -> skillId có sẵn
		Set<Long> distinctSkillIds = new LinkedHashSet<>();
		for (String name : skills) {
			if (name == null || name.isBlank()) continue;
			Optional<Skill> s = skillRepository.findAll().stream()
				.filter(sk -> sk.getName() != null && sk.getName().equalsIgnoreCase(name.trim()))
				.findFirst();
			s.ifPresent(skill -> distinctSkillIds.add(skill.getSkillId().longValue()));
		}
		if (distinctSkillIds.isEmpty()) {
			return jobPostingRepository.findAll()
				.stream()
				.sorted(Comparator.comparing(JobPosting::getPostedAt).reversed())
				.limit(limit)
				.collect(Collectors.toList());
		}
		// lấy job theo từng skillId, gộp, loại trùng, sắp xếp theo ngày đăng
		LinkedHashMap<Long, JobPosting> merged = new LinkedHashMap<>();
		for (Long skillId : distinctSkillIds) {
			List<JobPosting> items = jobPostingRepository.findBySkillSkillIdAndJobIdNot(skillId, -1L);
			for (JobPosting jp : items) {
				merged.putIfAbsent(jp.getJobId(), jp);
				if (merged.size() >= limit) break;
			}
			if (merged.size() >= limit) break;
		}
		return merged.values().stream()
			.sorted(Comparator.comparing(JobPosting::getPostedAt,
					Comparator.nullsLast(Comparator.naturalOrder())).reversed())
			.limit(limit)
			.collect(Collectors.toList());
	}
}


