package com.joblink.joblink.service;

import com.joblink.joblink.repository.BlogPostRepository;
import com.joblink.joblink.entity.BlogPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BlogPostService {
    @Autowired
    private BlogPostRepository blogPostRepository;

    public List<BlogPost> getAllPost() {
        return blogPostRepository.findAll();
    }

    public void softDeleteBlog(int id) {
        BlogPost blog = blogPostRepository.findById(id).orElseThrow();
        blog.setDeleted(!blog.isDeleted()); // Toggle deleted status
        blogPostRepository.save(blog);
    }

    public Optional<BlogPost> getPostById(int id) {
        return blogPostRepository.findById(id);
    }

    public BlogPost createPost(BlogPost post) {
        if (post.getCreatedAt() == null) {
            post.setCreatedAt(LocalDateTime.now());
        }
        post.setDeleted(false);
        return blogPostRepository.save(post);
    }

    public BlogPost updatePost(int id, BlogPost updated) {
        BlogPost existing = blogPostRepository.findById(id).orElseThrow();
        existing.setTitle(updated.getTitle());
        existing.setContent(updated.getContent());
        existing.setCategoryId(updated.getCategoryId());
        return blogPostRepository.save(existing);
    }

}
