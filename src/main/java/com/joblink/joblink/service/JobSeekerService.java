package com.joblink.joblink.service;



import com.joblink.joblink.entity.JobSeekerProfile;
import com.joblink.joblink.Repository.JobSeekerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobSeekerService implements IJobSeekerService {

    private final JobSeekerProfileRepository seekerRepository;

    @Override
    public JobSeekerProfile getByUserId(Integer userId) {
        return seekerRepository.findByUserId(userId)
                .orElse(null); // Controller có check null
    }
}