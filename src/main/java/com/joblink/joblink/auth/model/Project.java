package com.joblink.joblink.auth.model;

import lombok.Data;

import java.time.LocalDate;

/* ===== Project / Portfolio ===== */
@Data public class Project {
    private long id;
    private String name;
    private String role;
    private String url;
    private String techStack;   // chuỗi “Java, Spring, MySQL” …
    private String summary;     // mô tả ngắn
    private LocalDate startDate;
    private LocalDate endDate;
}
