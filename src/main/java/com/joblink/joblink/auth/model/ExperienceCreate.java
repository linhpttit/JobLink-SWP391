package com.joblink.joblink.auth.model;

import lombok.Data;

import java.time.LocalDate;

@Data public class ExperienceCreate {
    private String company;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private String description;
}
