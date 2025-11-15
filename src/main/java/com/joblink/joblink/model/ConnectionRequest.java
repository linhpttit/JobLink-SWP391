package com.joblink.joblink.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConnectionRequest {
    private Integer requestId;
    private Integer requesterSeekerId;
    private Integer targetSeekerId;
    private String status;
    private String message;
    private String commonSkills;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;

    // Additional fields for display
    private String requesterName;
    private String requesterAvatar;
    private String targetName;
    private String targetAvatar;
    private Integer requesterUserId;
    private Integer targetUserId;
}

