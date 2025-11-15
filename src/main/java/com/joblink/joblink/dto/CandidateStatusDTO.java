package com.joblink.joblink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateStatusDTO {
    private String status;  // submitted, reviewed, interviewed, hired, rejected
    private Long count;
    private String statusLabel; // Label hiển thị tiếng Việt
}
