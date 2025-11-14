package com.joblink.joblink.service;

import com.joblink.joblink.dao.SkillDao;
import com.joblink.joblink.entity.Skill;
import com.joblink.joblink.model.Skill2;
import com.joblink.joblink.Repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService implements ISkillService {
    private final SkillRepository skillRepository;
    private final SkillDao skillDao;

    @Override
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }
    public List<Skill2> getSkillsBySeekerId(int seekerId) {
        return skillDao.findBySeekerId(seekerId);
    }
}
