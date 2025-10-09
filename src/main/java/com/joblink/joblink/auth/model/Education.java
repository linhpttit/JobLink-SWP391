package com.joblink.joblink.auth.model;

import lombok.Data;

import java.time.LocalDate;

/* ===== Education ===== */
@Data public class Education {
    private long id;
    private String school;
    private String degree;
    private String fieldOfStudy;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}
