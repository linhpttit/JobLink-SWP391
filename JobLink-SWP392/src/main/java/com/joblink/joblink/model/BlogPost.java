package com.joblink.joblink.model;

public class BlogPost {
    private int postId;
    private String title;
    private String content;
    private String categoryName;
    private String createdAtFormatted;

    public BlogPost() {}

    public BlogPost(int postId, String title, String content,
                    String categoryName, String createdAtFormatted) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.categoryName = categoryName;
        this.createdAtFormatted = createdAtFormatted;
    }

    // Getters & Setters
    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getCreatedAtFormatted() { return createdAtFormatted; }
    public void setCreatedAtFormatted(String createdAtFormatted) { this.createdAtFormatted = createdAtFormatted; }
}