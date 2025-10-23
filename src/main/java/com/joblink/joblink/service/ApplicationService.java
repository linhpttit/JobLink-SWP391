
package com.joblink.joblink.service;

import com.joblink.joblink.dao.ApplicationDao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ApplicationService {
    private final ApplicationDao applicationDao;

    public ApplicationService(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    /**
     * Get list of companies that a seeker has applied to
     * Returns company info and application status
     */
    public List<Map<String, Object>> getAppliedCompaniesForSeeker(int seekerId) {
        return applicationDao.findAppliedCompaniesBySeeker(seekerId);
    }
}
