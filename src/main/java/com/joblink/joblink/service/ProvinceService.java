package com.joblink.joblink.service;

import com.joblink.joblink.entity.Province;
import com.joblink.joblink.Repository.ProvinceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProvinceService implements IProvinceService{
    private final ProvinceRepository provinceRepository;
    @Override
    public List<Province> getAllProvinces() {
        return provinceRepository.findAll();
    }
}
