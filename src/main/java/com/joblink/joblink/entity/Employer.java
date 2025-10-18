package com.joblink.joblink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity Employer - Map với bảng EmployerProfile trong DB
 */
@Entity
@Table(name = "EmployerProfile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employer_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "location", length = 150)
    private String location;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    // Lombok @Data tự động generate getters, setters, toString, equals, hashCode
}