package com.joblink.joblink.service;

import com.joblink.joblink.dto.*;
import com.joblink.joblink.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeService {

    @Autowired
    private HomeRepository homeRepository;

    public HomeStatsDTO getHomeStats() {
        return homeRepository.getHomeStats();
    }

    public List<CategoryStatsDTO> getPopularCategories() {
        return homeRepository.getPopularCategories();
    }

    public List<FeaturedJobDTO> getFeaturedJobs() {
        return homeRepository.getFeaturedJobs();
    }

    public List<TopCompanyDTO> getTopCompanies() {
        return homeRepository.getTopCompanies();
    }
}