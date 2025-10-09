package com.joblink.joblink.auth.model;

import java.util.List;

public class JobSeekerProfile {
    private int userId;
    private String fullName;
    private String username;
    private String email;

    // BỔ SUNG ↓↓↓
    private String avatarUrl;
    private String about;
    private String phone;
    private String address;
    private Integer experienceYears;

    private List<Skill> skills;
    private List<Experience> experiences;

    // --- getters/setters ---
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    public List<Skill> getSkills() { return skills; }
    public void setSkills(List<Skill> skills) { this.skills = skills; }

    public List<Experience> getExperiences() { return experiences; }
    public void setExperiences(List<Experience> experiences) { this.experiences = experiences; }
}
