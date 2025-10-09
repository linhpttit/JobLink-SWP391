package com.joblink.joblink.auth.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "EmployerProfile")
@Data
public class EmployerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employerId;

    private String companyName;
    private String industry;
    private String location;
    private String description;
    private String phoneNumber;

    @Transient
    private Long openPositions;
}
