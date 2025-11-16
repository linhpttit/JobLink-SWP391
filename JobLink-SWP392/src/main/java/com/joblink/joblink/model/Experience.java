package com.joblink.joblink.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Experience {
    private Integer experienceId;
    private Integer seekerId;
    private String jobTitle;
    private String companyName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String projectLink;
}
