package com.joblink.joblink.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployerProfileDto {
    private String companyName;
    private String address;
    private String phoneNumber;
    private String email;
    private String description;
    private String urlAvt;
    private String createdAt;
}
