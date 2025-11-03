package com.joblink.joblink.employer.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.joblink.joblink.entity.Skill;
import com.joblink.joblink.repository.SkillRepository;

@Service("NewSkillService")
public class SkillService implements ISkillService {
	private final SkillRepository repo;

	public SkillService(SkillRepository repo) {
		this.repo = repo;
	}

	@Override
	public List<Skill> findAll() {
		return repo.findAll();
	}

	@Override
	public Skill getById(Long id) {
		return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Ngành nghề không tồn tại"));
	}
}