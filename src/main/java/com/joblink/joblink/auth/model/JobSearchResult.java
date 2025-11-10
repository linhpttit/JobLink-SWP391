package com.joblink.joblink.auth.model;



import lombok.Data;

@Data
public class JobSearchResult {
    private int jobId;
    private String title;
    private String description;
    private String location;
    private Double salaryMin;
    private Double salaryMax;
    private String companyName;
    private String industry;
    private String allSkills;
    private int matchingSkillCount;
    private int totalCount;
    private int totalPages;
    private int currentPage;
}
