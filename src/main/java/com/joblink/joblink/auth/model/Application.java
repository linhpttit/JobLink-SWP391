package com.joblink.joblink.auth.model;



import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Application {
    private Integer applicationId;
    private int jobId;
    private int seekerId;
    private String status;
    private LocalDateTime appliedAt;
    private LocalDateTime lastStatusAt;
    private String cvUrl;
    private String note;
    private String statusLog; // Dạng JSON
}