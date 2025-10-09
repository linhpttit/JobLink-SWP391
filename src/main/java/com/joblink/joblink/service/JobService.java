package com.joblink.joblink.service;



import com.joblink.joblink.auth.model.JobSearchResult;
import com.joblink.joblink.Repository.JobRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class JobService {
    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public List<JobSearchResult> searchJobs(String skills, String location,
                                            Double minSalary, Double maxSalary,
                                            int page, int size) {
        return jobRepository.searchJobs(skills, location, minSalary, maxSalary, page, size);
    }
}
