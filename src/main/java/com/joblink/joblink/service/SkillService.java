package com.joblink.joblink.service;

import com.joblink.joblink.entity.Skill;
import com.joblink.joblink.Repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService implements ISkillService {
    private final SkillRepository skillRepository;
    @Override
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }
}
