package com.joblink.joblink.employer.application.service;

import java.util.List;

import com.joblink.joblink.entity.Skill;

public interface ISkillService {
	List<Skill> findAll();
    Skill getById(Long id);
}
