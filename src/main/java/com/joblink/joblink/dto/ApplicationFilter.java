package com.joblink.joblink.dto;

import java.util.List;

public class ApplicationFilter {
    private String search;
    private List<String> positions;
    private Integer minExperience;
    private Integer maxExperience;
    private List<String> statuses;
    private List<String> educationLevels;
    private String location;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "appliedAt";
    private String sortDirection = "DESC";

    // Getters and Setters
    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }

    public List<String> getPositions() { return positions; }
    public void setPositions(List<String> positions) { this.positions = positions; }

    public Integer getMinExperience() { return minExperience; }
    public void setMinExperience(Integer minExperience) { this.minExperience = minExperience; }

    public Integer getMaxExperience() { return maxExperience; }
    public void setMaxExperience(Integer maxExperience) { this.maxExperience = maxExperience; }

    public List<String> getStatuses() { return statuses; }
    public void setStatuses(List<String> statuses) { this.statuses = statuses; }

    public List<String> getEducationLevels() { return educationLevels; }
    public void setEducationLevels(List<String> educationLevels) { this.educationLevels = educationLevels; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortDirection() { return sortDirection; }
    public void setSortDirection(String sortDirection) { this.sortDirection = sortDirection; }
}