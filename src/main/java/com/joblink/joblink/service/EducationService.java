
package com.joblink.joblink.service;

import com.joblink.joblink.dao.EducationDao;
import com.joblink.joblink.model.Education;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EducationService {
    private final EducationDao educationDao;

    public EducationService(EducationDao educationDao) {
        this.educationDao = educationDao;
    }

    public List<Education> getEducationsBySeekerId(int seekerId) {
        return educationDao.findBySeekerId(seekerId);
    }
}
