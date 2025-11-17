package com.joblink.joblink.model;

public class Blog {
    private int blogId;
    private String title;
    private String content;
    private String categoryName;
    private String createdAtFormatted;
    private String updatedAtFormatted;
    private Integer parentBlogId;

    public Blog() {}

    public Blog(int blogId, String title, String content, String categoryName,
                String createdAtFormatted, String updatedAtFormatted, Integer parentBlogId) {
        this.blogId = blogId;
        this.title = title;
        this.content = content;
        this.categoryName = categoryName;
        this.createdAtFormatted = createdAtFormatted;
        this.updatedAtFormatted = updatedAtFormatted;
        this.parentBlogId = parentBlogId;
    }

    // Getters & Setters
    public int getBlogId() { return blogId; }
    public void setBlogId(int blogId) { this.blogId = blogId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getCreatedAtFormatted() { return createdAtFormatted; }
    public void setCreatedAtFormatted(String createdAtFormatted) { this.createdAtFormatted = createdAtFormatted; }

    public String getUpdatedAtFormatted() { return updatedAtFormatted; }
    public void setUpdatedAtFormatted(String updatedAtFormatted) { this.updatedAtFormatted = updatedAtFormatted; }

    public Integer getParentBlogId() { return parentBlogId; }
    public void setParentBlogId(Integer parentBlogId) { this.parentBlogId = parentBlogId; }
}