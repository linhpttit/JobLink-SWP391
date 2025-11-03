package com.joblink.joblink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.joblink.joblink.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

}
