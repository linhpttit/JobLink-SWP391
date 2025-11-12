package com.joblink.joblink.service.ai;

import java.util.List;
import java.util.Map;

public interface AIProviderClient {
	boolean isEnabled();
	AIResponse evaluateCVText(String prompt);

	class AIResponse {
		public Integer score;
		public List<String> strengths;
		public List<String> weaknesses;
		public List<String> advice;
		public List<String> skills;
		public String targetRoles;
		public String providerName;
		public String modelName;
		public String rawText;
		public Map<String, Object> rawDetails;
	}
}


