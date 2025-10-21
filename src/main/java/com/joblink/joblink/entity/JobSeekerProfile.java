package com.joblink.joblink.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity ánh xạ với bảng JobSeekerProfile
 */
@Entity
@Table(name = "JobSeekerProfile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobSeekerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seeker_id")
    private Integer seekerId;

    // Khóa ngoại trỏ đến bảng Users
    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId;

    // ✅ Sửa: [fullname] trong DB
    @Column(name = "fullname", columnDefinition = "NVARCHAR(255)")
    private String fullName;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "location", columnDefinition = "NVARCHAR(255)")
    private String location;

    @Column(name = "headline", columnDefinition = "NVARCHAR(255)")
    private String headline;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "about", columnDefinition = "NVARCHAR(MAX)")
    private String about;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    // ✅ Sửa lỗi: [dob] trong DB (Không phải date_of_birth)
    @Column(name = "dob")
    private LocalDate dateOfBirth;

    @Column(name = "avatar_url", columnDefinition = "NVARCHAR(MAX)")
    private String avatarUrl;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Cột [birthday] (Nếu dob đã là ngày sinh thì cột này có thể dư thừa,
    // nhưng tôi vẫn map nếu nó là cột riêng biệt)
    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "degree", columnDefinition = "NVARCHAR(100)")
    private String degree;

    @Column(name = "address", columnDefinition = "NVARCHAR(500)")
    private String address;

    @Column(name = "github_url", length = 255)
    private String githubUrl;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "completion_percentage")
    private Integer completionPercentage;

    @Column(name = "receive_invitations")
    private Boolean receiveInvitations;
}