package com.joblink.joblink.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResult {
    private Long jobId;
    private String title;
    private String companyName;
    private String location;
    private String salary;
    private String url;
}

