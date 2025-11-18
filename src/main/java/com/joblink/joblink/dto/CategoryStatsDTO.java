package com.joblink.joblink.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CategoryStatsDTO {
    private int categoryId;
    private String name;
    private int jobCount;

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getJobCount() {
        return jobCount;
    }

    public void setJobCount(int jobCount) {
        this.jobCount = jobCount;
    }
}