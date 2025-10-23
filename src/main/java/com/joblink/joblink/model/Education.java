package com.joblink.joblink.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Education {
    private Integer educationId;
    private Integer seekerId;
    private String university;
    private String degreeLevel; // Bachelor, Engineer, Doctorate, Master, Other
    private LocalDate startDate;
    private LocalDate graduationDate;
    private String description;
}
