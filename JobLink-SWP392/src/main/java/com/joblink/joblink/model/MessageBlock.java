
package com.joblink.joblink.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageBlock {
    private Integer blockId;
    private Integer blockerUserId;
    private Integer blockedUserId;
    private LocalDateTime blockedAt;
    private String reason;
}
