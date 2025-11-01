package com.joblink.joblink.Repository;

import com.joblink.joblink.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogRepository extends JpaRepository<Blog, Integer> {
    List<Blog> findAllByDeletedFalse();
}
