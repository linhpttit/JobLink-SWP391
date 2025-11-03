package com.joblink.joblink.employer.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.joblink.joblink.entity.District;
import com.joblink.joblink.repository.DistrictRepository;

@Service("NewDistrictService")
public class DistrictService implements IDistrictService {
	private final DistrictRepository repo;

	public DistrictService(DistrictRepository repo) {
		this.repo = repo;
	}

	@Override
	public List<District> findAll() {
		return repo.findAll();
	}

	@Override
	public List<District> findByProvinceId(Long provinceId) {
		return repo.findAll(); 
	}

	@Override
	public District getById(Long id) {
		return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Quận/Huyện không tồn tại"));
	}
}