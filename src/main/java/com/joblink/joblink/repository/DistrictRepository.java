package com.joblink.joblink.repository;

import com.joblink.joblink.entity.District;
import com.joblink.joblink.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DistrictRepository extends JpaRepository<District, Long> {
    Optional<District> findByDistrictId(Integer id);
}
