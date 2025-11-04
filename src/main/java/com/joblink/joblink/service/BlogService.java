package com.joblink.joblink.service;

import com.joblink.joblink.Repository.BlogRepository;
import com.joblink.joblink.entity.Blog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BlogService {

    @Autowired
    private BlogRepository blogRepository;

    public List<Blog> getAllBlogs() {
        return blogRepository.findAll();
    }

    public Optional<Blog> getBlogById(int id) {
        return blogRepository.findById(id);
    }

    public Blog updateBlog(int id, Blog updatedBlog) {
        Blog blog = blogRepository.findById(id).orElseThrow(() -> new RuntimeException("Blog not found"));
        blog.setTitle(updatedBlog.getTitle());
        blog.setContent(updatedBlog.getContent());
        blog.setCategoryId(updatedBlog.getCategoryId());
        blog.setParentBlogId(updatedBlog.getParentBlogId());
        blog.setUpdatedAt(LocalDateTime.now());
        return blogRepository.save(blog);
    }

    public Blog createBlog(Blog blog) {
        blog.setCreatedAt(LocalDateTime.now());
        blog.setDeleted(false);
        return blogRepository.save(blog);
    }

    public void softDeleteBlog(int id) {
        Blog blog = blogRepository.findById(id).orElseThrow();
        blog.setDeleted(!blog.isDeleted()); // Toggle deleted status
        blogRepository.save(blog);
    }

}
