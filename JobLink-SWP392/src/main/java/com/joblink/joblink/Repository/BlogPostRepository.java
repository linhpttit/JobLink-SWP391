package com.joblink.joblink.Repository;

import com.joblink.joblink.entity.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogPostRepository extends JpaRepository<BlogPost, Integer> {
    List<BlogPost> findAllByDeletedFalse();

}
