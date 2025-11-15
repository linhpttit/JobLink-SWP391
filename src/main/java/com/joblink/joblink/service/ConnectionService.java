package com.joblink.joblink.service;

import com.joblink.joblink.dao.ConnectionRequestDao;
import com.joblink.joblink.dao.ConversationDao;
import com.joblink.joblink.model.ConnectionRequest;
import com.joblink.joblink.model.Conversation;
import com.joblink.joblink.model.JobSeekerProfile2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConnectionService {
    private final ConnectionRequestDao connectionRequestDao;
    private final ConversationDao conversationDao;
    private final ProfileService profileService;
    private final PremiumService premiumService;

    public ConnectionService(ConnectionRequestDao connectionRequestDao,
                             ConversationDao conversationDao,
                             ProfileService profileService,
                             PremiumService premiumService) {
        this.connectionRequestDao = connectionRequestDao;
        this.conversationDao = conversationDao;
        this.profileService = profileService;
        this.premiumService = premiumService;
    }

    /** ✅ Lấy gợi ý theo user đang đăng nhập (đúng: userId -> profile -> seekerId) */
    public List<Map<String, Object>> getSuggestedConnections(int userId) {
        // LẤY PROFILE THEO userId (không phải seekerId)
        JobSeekerProfile2 profile = profileService.getOrCreateProfile(userId);
        if (profile == null) return new ArrayList<>();

        int mySeekerId = profile.getSeekerId();

        List<Integer> seekerIds = connectionRequestDao.findSeekersWithCommonSkills(mySeekerId);

        List<Map<String, Object>> suggestions = new ArrayList<>();
        for (Integer targetSeekerId : seekerIds) {
            // Bỏ qua nếu đã có request PENDING/ACCEPTED giữa hai bên
            ConnectionRequest existing = connectionRequestDao.findExistingRequest(mySeekerId, targetSeekerId);
            if (existing != null) continue;

            // Lấy danh sách kỹ năng chung
            List<String> commonSkills = connectionRequestDao.findCommonSkills(mySeekerId, targetSeekerId);
            if (commonSkills.isEmpty()) continue;

            JobSeekerProfile2 targetProfile = profileService.getProfileBySeekerId(targetSeekerId);
            if (targetProfile == null) continue;

            Map<String, Object> suggestion = new HashMap<>();
            suggestion.put("seekerId", targetSeekerId);
            suggestion.put("userId", targetProfile.getUserId());
            suggestion.put("fullname", targetProfile.getFullname());
            suggestion.put("headline", targetProfile.getHeadline());
            suggestion.put("avatarUrl", targetProfile.getAvatarUrl());
            suggestion.put("commonSkills", commonSkills);
            suggestion.put("commonSkillCount", commonSkills.size());

            suggestions.add(suggestion);
        }
        return suggestions;
    }

    @Transactional
    public ConnectionRequest sendConnectionRequest(int requesterSeekerId, int targetSeekerId, String message) {
        ConnectionRequest existing = connectionRequestDao.findExistingRequest(requesterSeekerId, targetSeekerId);
        if (existing != null) throw new IllegalStateException("Connection request already exists");

        List<String> commonSkills = connectionRequestDao.findCommonSkills(requesterSeekerId, targetSeekerId);
        String commonSkillsStr = String.join(", ", commonSkills);

        ConnectionRequest request = new ConnectionRequest();
        request.setRequesterSeekerId(requesterSeekerId);
        request.setTargetSeekerId(targetSeekerId);
        request.setStatus("PENDING");
        request.setMessage(message);
        request.setCommonSkills(commonSkillsStr);

        int requestId = connectionRequestDao.create(request);
        request.setRequestId(requestId);
        return request;
    }

    @Transactional
    public void acceptConnectionRequest(int requestId) {
        ConnectionRequest request = connectionRequestDao.findById(requestId);
        if (request == null) throw new IllegalArgumentException("Request not found");
        if (!"PENDING".equals(request.getStatus())) throw new IllegalStateException("Request is not pending");

        connectionRequestDao.updateStatus(requestId, "ACCEPTED");

        // Tạo hội thoại seeker-seeker nếu chưa có
        Conversation existingConv = conversationDao.findBySeekerPair(
                request.getRequesterSeekerId(), request.getTargetSeekerId());
        if (existingConv == null) {
            Conversation conversation = new Conversation();
            conversation.setSeekerId(request.getRequesterSeekerId());
            conversation.setSeekerId2(request.getTargetSeekerId());
            conversation.setConversationType("SEEKER_SEEKER");
            conversationDao.create(conversation);
        }
    }

    @Transactional
    public void rejectConnectionRequest(int requestId) {
        ConnectionRequest request = connectionRequestDao.findById(requestId);
        if (request == null) throw new IllegalArgumentException("Request not found");
        if (!"PENDING".equals(request.getStatus())) throw new IllegalStateException("Request is not pending");
        connectionRequestDao.updateStatus(requestId, "REJECTED");
    }

    public List<ConnectionRequest> getPendingRequests(int seekerId) {
        return connectionRequestDao.findPendingByTargetSeekerId(seekerId);
    }

    public List<ConnectionRequest> getSentRequests(int seekerId) {
        return connectionRequestDao.findByRequesterSeekerId(seekerId);
    }
}
