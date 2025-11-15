package com.joblink.joblink.controller;

import com.joblink.joblink.dto.UserSessionDTO;
import com.joblink.joblink.model.Conversation;
import com.joblink.joblink.model.JobSeekerProfile2;
import com.joblink.joblink.model.Message;
import com.joblink.joblink.service.MessageService;
import com.joblink.joblink.service.PremiumService;
import com.joblink.joblink.service.ProfileService;
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
    private final PremiumService premiumService;
    private final ProfileService profileService;

    public MessageController(MessageService messageService,
                             PremiumService premiumService,
                             ProfileService profileService) {
        this.messageService = messageService;
        this.premiumService = premiumService;
        this.profileService = profileService;
    }

    @GetMapping
    public String messagesPage(HttpSession session, Model model, RedirectAttributes ra) {
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập");
            return "redirect:/signin";
        }

        boolean hasMessagingAccess = premiumService.hasFeature(user.getUserId(), "messaging");
        if (!hasMessagingAccess && "seeker".equalsIgnoreCase(user.getRole())) {
            ra.addFlashAttribute("error", "Bạn cần nâng cấp lên gói Premium Titanium để sử dụng tính năng nhắn tin");
            return "redirect:/jobseeker/premium";
        }

        List<Conversation> conversations = messageService.getUserConversations(user.getUserId());
        int unreadCount = messageService.getUnreadMessageCount(user.getUserId());

        model.addAttribute("conversations", conversations);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("currentUserId", user.getUserId());
        model.addAttribute("hasMessagingAccess", hasMessagingAccess);

        return "messages";
    }

    @GetMapping("/conversation/{conversationId}")
    public String conversationPage(@PathVariable int conversationId,
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

        List<Message> messages = messageService.getConversationMessages(conversationId);
        messageService.markMessagesAsRead(conversationId, user.getUserId());

        model.addAttribute("conversation", conversation);
        model.addAttribute("messages", messages);
        model.addAttribute("currentUserId", user.getUserId());
        model.addAttribute("seekerId2", conversation.getSeekerId2());

        return "conversation";
    }

    /* ========== MỚI: Gửi tin dựa trên conversationId (dùng cho cả seeker-seeker & seeker-employer) ========== */
    @PostMapping("/send-in-conversation")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendInConversation(
            @RequestParam int conversationId,
            @RequestParam String content,
            HttpSession session) {

        Map<String, Object> res = new HashMap<>();
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            res.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(res);
        }

        Conversation conv = messageService.getConversation(conversationId);
        if (conv == null) {
            res.put("error", "Conversation not found");
            return ResponseEntity.badRequest().body(res);
        }

        try {
            Message msg;
            if ("SEEKER_SEEKER".equalsIgnoreCase(conv.getConversationType())) {
                // Xác định sender/receiver seekerId từ user hiện tại
                JobSeekerProfile2 p1 = profileService.getProfileBySeekerId(conv.getSeekerId());
                JobSeekerProfile2 p2 = profileService.getProfileBySeekerId(conv.getSeekerId2());
                if (p1 == null || p2 == null) {
                    res.put("error", "Profile not found");
                    return ResponseEntity.badRequest().body(res);
                }

                int senderSeekerId, receiverSeekerId, receiverUserId;
                if (p1.getUserId() != null && p1.getUserId().equals(user.getUserId())) {
                    senderSeekerId = p1.getSeekerId();
                    receiverSeekerId = p2.getSeekerId();
                    receiverUserId = p2.getUserId();
                } else {
                    senderSeekerId = p2.getSeekerId();
                    receiverSeekerId = p1.getSeekerId();
                    receiverUserId = p1.getUserId();
                }

                msg = messageService.sendSeekerToSeekerMessage(
                        user.getUserId(),
                        receiverUserId,
                        content,
                        "text",
                        senderSeekerId,
                        receiverSeekerId
                );
            } else {
                // SEEKER_EMPLOYER
                // Ở nhánh này, bạn đã có sẵn logic cũ dùng seekerId & employerId,
                // tuy nhiên từ conversation ta có đủ:
                Integer seekerId = conv.getSeekerId();
                Integer employerId = conv.getEmployerId();

                // Xác định receiverUserId: nếu current là seeker -> receiver là employer; ngược lại
                JobSeekerProfile2 seekerP = profileService.getProfileBySeekerId(seekerId);
                // EmployerProfile -> chưa có service ở đây, nên tạm thời yêu cầu client vẫn truyền receiverUserId khi là SEEKER_EMPLOYER
                // Nếu muốn hoàn thiện, thêm EmployerProfileService để map employerId -> userId.

                res.put("error", "SEND for SEEKER_EMPLOYER needs employer user mapping (chưa bật)");
                return ResponseEntity.status(501).body(res);
            }

            res.put("success", true);
            res.put("message", msg);
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    /* ========== MỚI: Lấy otherUserId theo conversationId (để block) ========== */
    @GetMapping("/api/conversation/{conversationId}/other-user")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOtherUser(@PathVariable int conversationId, HttpSession session) {
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).build();

        Conversation conv = messageService.getConversation(conversationId);
        if (conv == null) return ResponseEntity.notFound().build();

        Map<String, Object> res = new HashMap<>();
        if ("SEEKER_SEEKER".equalsIgnoreCase(conv.getConversationType())) {
            JobSeekerProfile2 p1 = profileService.getProfileBySeekerId(conv.getSeekerId());
            JobSeekerProfile2 p2 = profileService.getProfileBySeekerId(conv.getSeekerId2());
            int otherUserId = (p1.getUserId().equals(user.getUserId())) ? p2.getUserId() : p1.getUserId();
            res.put("otherUserId", otherUserId);
            return ResponseEntity.ok(res);
        } else {
            // TODO: employer mapping nếu cần
            return ResponseEntity.status(501).body(Map.of("error", "Not implemented for SEEKER_EMPLOYER"));
        }
    }

    /* ================== CÁC API CŨ GIỮ NGUYÊN (send, recall, block, unblock, list...) ================== */

    @PostMapping("/send")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestParam int seekerId2,
            @RequestParam String content,
            @RequestParam(required = false, defaultValue = "text") String messageType,
            @RequestParam int seekerId,
            @RequestParam int employerId,
            @RequestParam int userId,
            @RequestParam int userId2,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");

        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(response);
        }

        if ("seeker".equalsIgnoreCase(user.getRole())) {
            boolean hasMessagingAccess = premiumService.hasFeature(user.getUserId(), "messaging");
            if (!hasMessagingAccess) {
                response.put("error", "Bạn cần nâng cấp lên gói Premium Titanium để sử dụng tính năng nhắn tin");
                return ResponseEntity.status(403).body(response);
            }
        }

        try {
            Message message = messageService.sendMessage(
                    user.getUserId(),
                    seekerId2,
                    content,
                    messageType,
                    seekerId,
                    employerId,
                    userId,
                    userId2
            );
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
    public ResponseEntity<Map<String, Object>> recallMessage(@PathVariable int messageId, HttpSession session) {
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
    public ResponseEntity<Map<String, Object>> blockUser(@RequestParam int blockedUserId,
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
    public ResponseEntity<Map<String, Object>> unblockUser(@RequestParam int blockedUserId, HttpSession session) {
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
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(messageService.getUserConversations(user.getUserId()));
    }

    @GetMapping("/api/conversation/{conversationId}/messages")
    @ResponseBody
    public ResponseEntity<List<Message>> getMessages(@PathVariable int conversationId, HttpSession session) {
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(messageService.getConversationMessages(conversationId));
    }

    @GetMapping("/api/unread-count")
    @ResponseBody
    public ResponseEntity<Map<String, Integer>> getUnreadCount(HttpSession session) {
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).build();
        Map<String, Integer> response = new HashMap<>();
        response.put("count", messageService.getUnreadMessageCount(user.getUserId()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-seeker")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendSeekerMessage(@RequestParam int receiverUserId,
                                                                 @RequestParam String content,
                                                                 @RequestParam(required = false, defaultValue = "text") String messageType,
                                                                 @RequestParam int senderSeekerId,
                                                                 @RequestParam int receiverSeekerId,
                                                                 HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            response.put("error", "Vui lòng đăng nhập");
            return ResponseEntity.badRequest().body(response);
        }

        boolean hasNetworkingAccess = premiumService.hasFeature(user.getUserId(), "networking");
        if (!hasNetworkingAccess) {
            response.put("error", "Bạn cần nâng cấp lên gói Premium Titanium để nhắn tin với người tìm việc khác");
            return ResponseEntity.status(403).body(response);
        }

        try {
            Message message = messageService.sendSeekerToSeekerMessage(
                    user.getUserId(),
                    receiverUserId,
                    content,
                    messageType,
                    senderSeekerId,
                    receiverSeekerId
            );
            response.put("success", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}