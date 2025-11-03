package com.joblink.joblink.employer.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.joblink.joblink.entity.Province;
import com.joblink.joblink.repository.ProvinceRepository;

@Service("NewProvinceService")
public class ProvinceService implements IProvinceService {
	private final ProvinceRepository repo;

	public ProvinceService(ProvinceRepository repo) {
		this.repo = repo;
	}

	@Override
	public List<Province> findAll() {
		return repo.findAll();
	}

	@Override
	public Province getById(Long id) {
		return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Tỉnh/Thành không tồn tại"));
	}
}