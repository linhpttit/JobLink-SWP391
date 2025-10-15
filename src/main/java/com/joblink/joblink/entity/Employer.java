package com.joblink.joblink.entity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "EmployerProfile", schema = "dbo")
public class Employer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employer_id")
    private Long id;

    // Quan hệ 1-1 với User
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(length = 100)
    private String industry;

    @Column(length = 150)
    private String location;

    @Lob
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
}

