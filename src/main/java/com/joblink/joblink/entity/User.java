package com.joblink.joblink.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "Users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "role", nullable = false, length = 30)
    private String role;

    @Column(name = "url_avt", length = 500)
    private String urlAvt;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "google_id", length = 255)
    private String googleId;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY là mặc định, EAGER nếu muốn luôn tải User cùng Invoice
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false) // Map vào cột user_id, không cho insert/update qua đây
    private User user;
}