
package com.joblink.joblink.service;

import com.joblink.joblink.dao.ExperienceDao;
import com.joblink.joblink.model.Experience;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExperienceService {
    private final ExperienceDao experienceDao;

    public ExperienceService(ExperienceDao experienceDao) {
        this.experienceDao = experienceDao;
    }

    public List<Experience> getExperiencesBySeekerId(int seekerId) {
        return experienceDao.findBySeekerId(seekerId);
    }
}
