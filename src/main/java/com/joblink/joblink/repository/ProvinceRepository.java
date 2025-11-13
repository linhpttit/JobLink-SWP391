package com.joblink.joblink.repository;

import com.joblink.joblink.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProvinceRepository extends JpaRepository<Province, Long> {
    Optional<Province> findByProvinceId(Integer id);
}
