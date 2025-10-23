
package com.joblink.joblink.controller;

import com.joblink.joblink.model.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class WebSocketMessageController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketMessageController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload Message message) {
        // Send to specific user's queue
        messagingTemplate.convertAndSendToUser(
                String.valueOf(message.getMessageId()),
                "/queue/messages",
                message
        );
    }

    @MessageMapping("/chat.recallMessage")
    public void recallMessage(@Payload Map<String, Object> payload) {
        Integer messageId = (Integer) payload.get("messageId");
        Integer receiverUserId = (Integer) payload.get("receiverUserId");

        messagingTemplate.convertAndSendToUser(
                String.valueOf(receiverUserId),
                "/queue/recall",
                payload
        );
    }
}
