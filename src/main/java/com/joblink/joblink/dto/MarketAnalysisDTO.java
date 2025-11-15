package com.joblink.joblink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketAnalysisDTO {
    private String position;
    
    // Thống kê tuyển dụng
    private Long totalJobs;              // Tổng số tin tuyển dụng
    private Long activeJobs;             // Số tin đang active
    private Double growthRate;           // Tỷ lệ tăng trưởng so với kỳ trước (%)
    
    // Thống kê mức lương
    private Double avgSalaryMin;         // Mức lương tối thiểu trung bình
    private Double avgSalaryMax;         // Mức lương tối đa trung bình
    private Double avgSalary;            // Mức lương trung bình
    
    // Thống kê cạnh tranh
    private Long totalApplicants;        // Tổng số ứng viên ứng tuyển
    private Double avgApplicantsPerJob;  // Số ứng viên trung bình mỗi tin
    private String competitionLevel;     // Mức độ cạnh tranh: "Thấp", "Trung bình", "Cao"
    
    // Thống kê kinh nghiệm yêu cầu
    private String mostCommonExperience; // Yêu cầu kinh nghiệm phổ biến nhất
    
    // Thống kê địa điểm
    private String topLocation;          // Địa điểm có nhiều tin nhất
}
