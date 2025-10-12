
package com.joblink.joblink.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Conversation {
    private Integer conversationId;
    private Integer seekerId;
    private Integer employerId;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;

    // Additional fields for display
    private String otherUserName;
    private String otherUserAvatar;
    private Integer otherUserId;
    private String lastMessageContent;
    private Integer unreadCount;
    private Boolean isBlocked;
}
