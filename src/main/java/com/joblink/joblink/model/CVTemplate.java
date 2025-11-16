
package com.joblink.joblink.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CVTemplate {
    private Integer templateId;
    private String templateName;
    private String templateCode;
    private String description;
    private String thumbnailUrl;
    private String htmlContent;
    private String cssContent;
    private String category;
    private Boolean isPremium;
    private Boolean isActive;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
