package com.joblink.joblink.auth.model;



import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BlockedEmployer {
    private Integer blockId;
    private int seekerId;
    private int employerId;
    private LocalDateTime createdAt;
}