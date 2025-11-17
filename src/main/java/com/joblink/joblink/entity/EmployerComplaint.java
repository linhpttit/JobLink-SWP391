package com.joblink.joblink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "EmployerComplaints")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployerComplaint {
    @Id
    @Column(name = "complaint_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_seeker_id")
    private JobSeekerProfile jobSeeker;

    @ManyToOne
    @JoinColumn(name = "employer_id")
    private Employer employer;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String status; // PENDING / RESOLVED / REJECTED

    @Column(columnDefinition = "TEXT")
    private String response;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDate.now();
        status = "PENDING";
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDate.now();
    }
}
