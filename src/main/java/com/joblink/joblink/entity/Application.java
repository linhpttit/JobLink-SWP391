package com.joblink.joblink.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Applications")
@Data
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "job_id", nullable = false)
    private Integer jobId;  // Chỉ lưu ID, không dùng @ManyToOne

    @Column(name = "seeker_id", nullable = false)
    private Integer seekerId;  // Chỉ lưu ID, không dùng @ManyToOne

    @Column(name = "status", length = 20, nullable = false)
    private String status = "submitted";

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @Column(name = "last_status_at")
    private LocalDateTime lastStatusAt;

    @Column(name = "cv_url", length = 500)
    private String cvUrl;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "status_log", columnDefinition = "NVARCHAR(MAX)")
    private String statusLog;
}