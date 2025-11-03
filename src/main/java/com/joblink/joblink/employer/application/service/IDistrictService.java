package com.joblink.joblink.employer.application.service;

import java.util.List;

import com.joblink.joblink.entity.District;

public interface IDistrictService {
	List<District> findAll();

	List<District> findByProvinceId(Long provinceId);

	District getById(Long id);
}
