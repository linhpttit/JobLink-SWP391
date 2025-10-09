package com.joblink.joblink.service;

import com.joblink.joblink.Repository.EmployerRepository;
import com.joblink.joblink.auth.model.EmployerProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployerService {
    private final EmployerRepository employerRepository;

    public List<EmployerProfile> searchOpenEmployers(String keyword, String location, String industry, String sortBy) {
        List<Object[]> rows = employerRepository.findEmployersWithOpenJobs(keyword, location, industry, sortBy);
        List<EmployerProfile> employers = new ArrayList<>();

        for (Object[] r : rows) {
            EmployerProfile e = new EmployerProfile();
            e.setEmployerId(((Number) r[0]).longValue());
            e.setCompanyName((String) r[1]);
            e.setIndustry((String) r[2]);
            e.setLocation((String) r[3]);
            e.setDescription((String) r[4]);
            e.setPhoneNumber((String) r[5]);
            e.setOpenPositions(((Number) r[7]).longValue()); // cột open_positions
            employers.add(e);
        }

        return employers;
    }
}
