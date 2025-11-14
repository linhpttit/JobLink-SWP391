
package com.joblink.joblink.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Message {
    private Integer messageId;
    private Integer conversationId;
    private Integer seekerId;
    private Integer seekerId2;
    private Integer userId;
    private Integer userId2;
    private String messageContent;
    private String messageType; // 'text', 'address', 'system'
    private Boolean isRead;
    private Boolean isRecalled;
    private LocalDateTime recalledAt;
    private LocalDateTime sentAt;

    // Additional fields for display
    private String senderName;
    private String senderAvatar;
    private String receiverName;
}