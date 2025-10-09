package com.joblink.joblink.auth.model;

import lombok.Data;

import java.time.LocalDate;

@Data public class EducationCreate {
    private String school;
    private String degree;
    private String fieldOfStudy;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}
