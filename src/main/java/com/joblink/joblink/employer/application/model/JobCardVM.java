package com.joblink.joblink.employer.application.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class JobCardVM {
    Long jobId;
    String title;
    String position;
    String provinceName;
    String yearExperience;       
    String workType;
    String salaryText;
    String postedAtText;
    String submissionDeadlineText;  
    int applicationsCount;
}