package com.joblink.joblink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateRecommendationDTO {
    private Integer seekerId;
    private String fullname;
    private String avatarUrl;
    private Integer experienceYears;
    private String headline;
    private List<String> skills;
    private Integer matchingSkillCount;
    private Integer totalSkillCount;
    private Integer matchPercentage;
    private Boolean experienceMatch;
    
    // Helper method để tạo initials cho avatar
    public String getInitials() {
        if (fullname == null || fullname.trim().isEmpty()) {
            return "??";
        }
        String[] parts = fullname.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
