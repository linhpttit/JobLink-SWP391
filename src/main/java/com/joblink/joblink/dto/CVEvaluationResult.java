package com.joblink.joblink.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class CVEvaluationResult {
	private Integer cvId;
	private Integer seekerId;
	private Integer overallScore; // 0-100
	private List<String> strengths;
	private List<String> weaknesses;
	private List<String> improvementAdvice;
	private List<String> extractedSkills; // kỹ năng chuẩn hoá từ CV/cover letter
	private String targetRoles; // roles gợi ý ngắn gọn
	private Map<String, Object> providerDetails; // chi tiết nhà cung cấp/model
}


