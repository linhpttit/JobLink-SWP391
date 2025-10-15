package com.joblink.joblink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "JobsPosting", schema = "dbo")
public class JobPosting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "status", length = 50)
    private String status = "ACTIVE";

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "position", nullable = false, length = 150)
    private String position;

    @Column(name = "street_address", length = 255)
    private String streetAddress;

    @Column(name = "year_experience", nullable = false, length = 150)
    private String yearExperience;

    @Column(name = "hiring_number", nullable = false)
    private Integer hiringNumber;

    @Column(name = "submission_deadline", nullable = false)
    private LocalDate submissionDeadline;

    @Column(name = "work_type", nullable = false, length = 255)
    private String workType;

    @Column(name = "salary_min", precision = 12, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 12, scale = 2)
    private BigDecimal salaryMax;

    @Lob
    @Column(name = "job_desc", nullable = false)
    private String jobDescription;

    @Lob
    @Column(name = "job_requirements", nullable = false)
    private String jobRequirements;

    @Lob
    @Column(name = "benefits", nullable = false)
    private String benefits;

    @Column(name = "contact_name", nullable = false, length = 255)
    private String contactName;

    @Column(name = "contact_email", nullable = false, length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", nullable = false, length = 10)
    private String contactPhone;

    @Column(name = "posted_at", nullable = false)
    private LocalDate postedAt = LocalDate.now();

    // ===================== RELATIONSHIPS =====================

    @ManyToOne
    @JoinColumn(name = "employer_id", nullable = false)
    private Employer employer;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;

    @ManyToOne
    @JoinColumn(name = "district_id")
    private District district;
}
