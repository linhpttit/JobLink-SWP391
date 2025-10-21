package com.joblink.joblink.controller;



import com.joblink.joblink.auth.model.JobSearchResult;
import com.joblink.joblink.service.JobService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobSearchController {

    private final JobService jobService;

    public JobSearchController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/search")
    public List<JobSearchResult> searchJobs(
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {

        return jobService.searchJobs(skills, location, minSalary, maxSalary, page, size);
    }
}
