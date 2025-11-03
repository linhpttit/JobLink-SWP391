package com.joblink.joblink.service;

import com.joblink.joblink.entity.District;
import com.joblink.joblink.repository.DistrictRepository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DistrictService implements IDistrictService{
    private final DistrictRepository districtRepository;
    @Override
    public List<District> getAllDistricts() {
        return districtRepository.findAll();
    }
}
