
package com.joblink.joblink.controller;

import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.model.Conversation;
import com.joblink.joblink.model.JobSeekerProfile;
import com.joblink.joblink.model.Message;
import com.joblink.joblink.service.JobSeekerService;
import com.joblink.joblink.service.MessageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

        import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/jobseeker/chat")
public class ChatController {

    private final MessageService messageService;
    private final JobSeekerService jobSeekerService;

    public ChatController(MessageService messageService, JobSeekerService jobSeekerService) {
        this.messageService = messageService;
        this.jobSeekerService = jobSeekerService;
    }

    @GetMapping("/{otherSeekerId}")
    public String chatPage(@PathVariable int otherSeekerId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"seeker".equalsIgnoreCase(user.getRole())) {
            return "redirect:/signin";
        }

        // Get current user's profile
        JobSeekerProfile currentProfile = jobSeekerService.getProfileByUserId(user.getUserId());

        // Get other user's profile
        JobSeekerProfile otherProfile = jobSeekerService.getProfileBySeekerId(otherSeekerId);
        if (otherProfile == null) {
            return "redirect:/jobseeker/networking";
        }

        model.addAttribute("user", user);
        model.addAttribute("currentProfile", currentProfile);
        model.addAttribute("otherProfile", otherProfile);
        model.addAttribute("otherSeekerId", otherSeekerId);

        return "chat";
    }

    @GetMapping("/messages/{otherSeekerId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMessages(@PathVariable int otherSeekerId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        JobSeekerProfile currentProfile = jobSeekerService.getProfileByUserId(user.getUserId());
        JobSeekerProfile otherProfile = jobSeekerService.getProfileBySeekerId(otherSeekerId);

        // Get conversation
        Conversation conversation = messageService.getSeekerConversation(currentProfile.getSeekerId(), otherSeekerId);

        List<Message> messages = List.of();
        if (conversation != null) {
            messages = messageService.getConversationMessages(conversation.getConversationId());
            // Mark messages as read
            messageService.markMessagesAsRead(conversation.getConversationId(), user.getUserId());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("messages", messages);
        response.put("currentUserId", user.getUserId());
        response.put("otherUser", Map.of(
                "seekerId", otherProfile.getSeekerId(),
                "fullname", otherProfile.getFullname(),
                "avatarUrl", otherProfile.getAvatarUrl() != null ? otherProfile.getAvatarUrl() : "/images/user.png"
        ));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/send")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestParam int receiverSeekerId,
            @RequestParam String content,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            JobSeekerProfile senderProfile = jobSeekerService.getProfileByUserId(user.getUserId());
            JobSeekerProfile receiverProfile = jobSeekerService.getProfileBySeekerId(receiverSeekerId);

            Message message = messageService.sendSeekerToSeekerMessage(
                    user.getUserId(),
                    receiverProfile.getUserId(),
                    content,
                    "text",
                    senderProfile.getSeekerId(),
                    receiverSeekerId
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
