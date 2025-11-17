package com.joblink.joblink.util;

import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.dto.UserSessionDTO;
import com.joblink.joblink.model.JobSeekerProfile2;
import com.joblink.joblink.service.ProfileService;
import org.springframework.stereotype.Component;

@Component
public class SessionHelper {

    private final ProfileService profileService;

    public SessionHelper(ProfileService profileService) {
        this.profileService = profileService;
    }

    public UserSessionDTO toSessionDTO(User user) {
        if (user == null) return null;
        UserSessionDTO dto = new UserSessionDTO();
        dto.setUserId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());

        // Try to enrich for seekers
        if (user.getRole() != null && "seeker".equalsIgnoreCase(user.getRole())) {
            try {
                JobSeekerProfile2 profile = profileService.getOrCreateProfile(user.getUserId());
                if (profile != null) {
                    dto.setFullName(profile.getFullname());
                    dto.setAvatarUrl(profile.getAvatarUrl());
                }
            } catch (Exception ignored) {
            }
        }

        if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
            dto.setFullName(user.getUsername() != null ? user.getUsername() : user.getEmail());
        }

        return dto;
    }
}
