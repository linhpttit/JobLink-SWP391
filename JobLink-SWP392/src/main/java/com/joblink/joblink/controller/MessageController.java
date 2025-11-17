
package com.joblink.joblink.controller;

// domain User import removed; controllers read UserSessionDTO from session
import com.joblink.joblink.dto.UserSessionDTO;
import com.joblink.joblink.model.Conversation;
import com.joblink.joblink.model.Message;
import com.joblink.joblink.service.MessageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
        import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public String messagesPage(HttpSession session, Model model, RedirectAttributes ra) {
    UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập");
            return "redirect:/signin";
        }

        List<Conversation> conversations = messageService.getUserConversations(user.getUserId());
        int unreadCount = messageService.getUnreadMessageCount(user.getUserId());

        model.addAttribute("conversations", conversations);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("currentUserId", user.getUserId());

        return "messages";
    }

    @GetMapping("/conversation/{conversationId}")
    public String conversationPage(
            @PathVariable int conversationId,
            HttpSession session,
            Model model,
            RedirectAttributes ra) {

    UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập");
            return "redirect:/signin";
        }

        Conversation conversation = messageService.getConversation(conversationId);
        if (conversation == null) {
            ra.addFlashAttribute("error", "Conversation not found");
            return "redirect:/messages";
        }

        // Enrich conversation with otherUserId and other details from getUserConversations
        List<Conversation> enrichedConversations = messageService.getUserConversations(user.getUserId());
        Conversation enrichedConversation = enrichedConversations.stream()
                .filter(c -> c.getConversationId() != null && c.getConversationId().equals(conversationId))
                .findFirst()
                .orElse(null);
        
        // If not found, use basic conversation (shouldn't happen but fallback)
        if (enrichedConversation == null) {
            enrichedConversation = conversation;
        }

        List<Message> messages = messageService.getConversationMessages(conversationId);
        messageService.markMessagesAsRead(conversationId, user.getUserId());

        model.addAttribute("conversation", enrichedConversation);
        model.addAttribute("messages", messages);
        model.addAttribute("currentUserId", user.getUserId());

        return "conversation";
    }

    @PostMapping("/send")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestParam int receiverUserId,
            @RequestParam String content,
            @RequestParam(required = false, defaultValue = "text") String messageType,
            @RequestParam(required = false) Integer seekerId,
            @RequestParam(required = false) Integer employerId,
            @RequestParam(required = false) Integer seekerId2,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Message message;
            
            // Determine conversation type and send appropriate message
            if (seekerId2 != null && seekerId != null) {
                // SEEKER_SEEKER conversation - use seekerId and seekerId2
                message = messageService.sendSeekerToSeekerMessage(
                        user.getUserId(),
                        receiverUserId,
                        content,
                        messageType,
                        seekerId,
                        seekerId2
                );
            } else if (seekerId != null && employerId != null) {
                // SEEKER_EMPLOYER conversation - use seekerId and employerId
                message = messageService.sendMessage(
                        user.getUserId(),
                        receiverUserId,
                        content,
                        messageType,
                        seekerId,
                        employerId
                );
            } else {
                response.put("error", "Invalid conversation parameters. Need seekerId+seekerId2 for SEEKER_SEEKER or seekerId+employerId for SEEKER_EMPLOYER");
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("success", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/recall/{messageId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> recallMessage(
            @PathVariable int messageId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
    UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            messageService.recallMessage(messageId, user.getUserId());
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/block")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> blockUser(
            @RequestParam int blockedUserId,
            @RequestParam(required = false) String reason,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
    UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            messageService.blockUser(user.getUserId(), blockedUserId, reason);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/unblock")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unblockUser(
            @RequestParam int blockedUserId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
    UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            messageService.unblockUser(user.getUserId(), blockedUserId);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/api/conversations")
    @ResponseBody
    public ResponseEntity<List<Conversation>> getConversations(HttpSession session) {
    UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(messageService.getUserConversations(user.getUserId()));
    }

    @GetMapping("/api/conversation/{conversationId}/messages")
    @ResponseBody
    public ResponseEntity<List<Message>> getMessages(
            @PathVariable int conversationId,
            HttpSession session) {

        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(messageService.getConversationMessages(conversationId));
    }

    @GetMapping("/api/unread-count")
    @ResponseBody
    public ResponseEntity<Map<String, Integer>> getUnreadCount(HttpSession session) {
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        Map<String, Integer> response = new HashMap<>();
        response.put("count", messageService.getUnreadMessageCount(user.getUserId()));
        return ResponseEntity.ok(response);
    }
}
