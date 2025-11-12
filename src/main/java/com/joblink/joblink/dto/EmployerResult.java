package com.joblink.joblink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerResult {
    private long employerId;
    private String companyName;
    private String industry;
    private String location;
    private String description;
    private String url;
}
