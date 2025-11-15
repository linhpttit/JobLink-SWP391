package com.joblink.joblink.Repository;

import com.joblink.joblink.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CVUploadRepository extends JpaRepository<Blog, Integer> {
}