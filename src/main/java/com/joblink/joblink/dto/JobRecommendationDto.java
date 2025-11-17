package com.joblink.joblink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRecommendationDto {
	private Long jobId;
	private String title;
	private String companyName;
	private String provinceName;
	private LocalDateTime postedAt;
}


