
package com.joblink.joblink.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CVUpload {
    private Integer cvId;
    private Integer seekerId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String preferredLocation;
    private Integer yearsOfExperience;
    private String currentJobLevel;
    private String workMode;
    private String expectedSalary;
    private String currentSalary;
    private String coverLetter;
    private String cvFileUrl;
    private String cvFileName;
    private LocalDateTime uploadedAt;
}
