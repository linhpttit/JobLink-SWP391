package com.joblink.joblink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private Long totalJobs;
    private Long totalApplications;
    private Long pendingReview;
    private Long reviewedCount;
    private Long rejectedCount;
    private Long acceptedCount;
    
    // Phần trăm thay đổi so với kỳ trước
    private Double jobsChangePercent;
    private Double applicationsChangePercent;
    private Double pendingChangePercent;
    private Double reviewedChangePercent;
    private Double rejectedChangePercent;
    private Double acceptedChangePercent;
}
