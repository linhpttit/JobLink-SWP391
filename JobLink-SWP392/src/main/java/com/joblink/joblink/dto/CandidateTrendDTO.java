package com.joblink.joblink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateTrendDTO {
    private String period;  // "Tuần 1", "Tháng 1", etc.
    private Long candidateCount;
    private String periodLabel; // Label hiển thị trên chart
}
