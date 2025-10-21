package com.joblink.joblink.service;

import com.joblink.joblink.entity.JobSeekerProfile;

public interface IJobSeekerService {
    JobSeekerProfile getByUserId(Integer userId);
}