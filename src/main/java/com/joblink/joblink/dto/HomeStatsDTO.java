package com.joblink.joblink.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HomeStatsDTO {
    private int liveJobs;
    private int companies;
    private int candidates;
    private int newJobs;

    public int getLiveJobs() {
        return liveJobs;
    }

    public void setLiveJobs(int liveJobs) {
        this.liveJobs = liveJobs;
    }

    public int getCompanies() {
        return companies;
    }

    public void setCompanies(int companies) {
        this.companies = companies;
    }

    public int getCandidates() {
        return candidates;
    }

    public void setCandidates(int candidates) {
        this.candidates = candidates;
    }

    public int getNewJobs() {
        return newJobs;
    }

    public void setNewJobs(int newJobs) {
        this.newJobs = newJobs;
    }
}