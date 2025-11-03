package com.joblink.joblink.employer.application.service;

import java.util.List;

import com.joblink.joblink.entity.Province;

public interface IProvinceService {
	List<Province> findAll();

	Province getById(Long id);
}
