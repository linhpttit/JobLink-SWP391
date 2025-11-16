package com.joblink.joblink.service;

import com.joblink.joblink.dao.DashboardDao;
import  com.joblink.joblink.dao.JobSeekerProfileDao;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class DashBoardJobSeeker {
    private final DashboardDao dashboardDao;
    private final JobSeekerProfileDao jobSeekerProfileDao;


    public DashBoardJobSeeker(DashboardDao dashboardDao, JobSeekerProfileDao jobSeekerProfileDao) {
        this.dashboardDao = dashboardDao;
        this.jobSeekerProfileDao = jobSeekerProfileDao;
    }

    public Map<String, Object> getCompleteDashboardData(int seekerId) {
        Map<String, Object> dashboardData = new java.util.HashMap<>();
        dashboardData.put("profile", dashboardDao.getUserProfileInfo(seekerId));
        dashboardData.put("mostRecentCV", dashboardDao.getMostRecentCV(seekerId));
        dashboardData.put("statistics", dashboardDao.getStatistics(seekerId));
        return dashboardData;
    }

    public Map<String, Object> getDashboardStatistics(int seekerId) {
        return dashboardDao.getStatistics(seekerId);
    }
}