
// ============================================
// 1. ChatbotController.java
// ============================================
package com.joblink.joblink.controller;

import com.joblink.joblink.dto.ChatRequest;
import com.joblink.joblink.dto.ChatResponse;
import com.joblink.joblink.service.ai.AIChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/chatbot")
@RequiredArgsConstructor
public class SeekerChatBotController {

    private final AIChatbotService chatbotService;

    @GetMapping
    public String chatbotPage() {
        return "SeekerChatBot";
    }

    @PostMapping("/ask")
    @ResponseBody
    public ResponseEntity<ChatResponse> askQuestion(@RequestBody ChatRequest request) {
        try {
            ChatResponse response = chatbotService.processQuestion(request.getQuestion());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(ChatResponse.builder()
                    .message("Xin lỗi, tôi gặp sự cố khi xử lý câu hỏi của bạn. Vui lòng thử lại!")
                    .type("error")
                    .build());
        }
    }
}