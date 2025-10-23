
package com.joblink.joblink.service;

import com.joblink.joblink.dao.SkillDao;
import com.joblink.joblink.model.Skill;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkillService {
    private final SkillDao skillDao;

    public SkillService(SkillDao skillDao) {
        this.skillDao = skillDao;
    }

    public List<Skill> getSkillsBySeekerId(int seekerId) {
        return skillDao.findBySeekerId(seekerId);
    }
}
