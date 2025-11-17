package com.joblink.joblink.service.ai;

import com.joblink.joblink.dto.CVEvaluationResult;
import com.joblink.joblink.model.CVUpload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CVEvaluationService {
	private final OpenAIClient openAIClient;
	private final GeminiClient geminiClient;
	private final com.joblink.joblink.service.CVTextExtractor cvTextExtractor;

	public CVEvaluationResult evaluate(CVUpload cv) {
		String normalized = buildPrompt(cv);
		List<AIProviderClient.AIResponse> responses = new ArrayList<>();

		if (openAIClient.isEnabled()) {
			responses.add(openAIClient.evaluateCVText(normalized));
		}
		if (geminiClient.isEnabled()) {
			responses.add(geminiClient.evaluateCVText(normalized));
		}

		// nếu không có provider nào có key, trả về khuyến cáo baseline
		if (responses.isEmpty()) {
			return baseline(cv);
		}

		return ensemble(cv, responses);
	}

	private String buildPrompt(CVUpload cv) {
		String formText = """
			FullName: %s
			Email: %s, Phone: %s
			PreferredLocation: %s
			YearsOfExperience: %s
			CurrentJobLevel: %s
			WorkMode: %s
			ExpectedSalary: %s
			CurrentSalary: %s
			CoverLetter (from form):
			%s
			""".formatted(
			nullToEmpty(cv.getFullName()),
			nullToEmpty(cv.getEmail()), nullToEmpty(cv.getPhoneNumber()),
			nullToEmpty(cv.getPreferredLocation()),
			cv.getYearsOfExperience() == null ? "" : cv.getYearsOfExperience().toString(),
			nullToEmpty(cv.getCurrentJobLevel()),
			nullToEmpty(cv.getWorkMode()),
			nullToEmpty(cv.getExpectedSalary()),
			nullToEmpty(cv.getCurrentSalary()),
			nullToEmpty(cv.getCoverLetter())
		);
		// Kết hợp nội dung CV file (nếu có)
		String fileText = cvTextExtractor.extractFromUrl(cv.getCvFileUrl());
		if (fileText != null && !fileText.isBlank()) {
			// Giới hạn độ dài để tránh prompt quá lớn
			int max = 10000;
			String clipped = fileText.length() > max ? fileText.substring(0, max) : fileText;
			return formText + "\n---\nCV File Text (extracted):\n" + clipped;
		}
		return formText;
	}

	private String nullToEmpty(String s) {
		return s == null ? "" : s;
	}

	private CVEvaluationResult baseline(CVUpload cv) {
		List<String> strengths = new ArrayList<>();
		List<String> weaknesses = new ArrayList<>();
		List<String> advice = new ArrayList<>();

		if (cv.getEmail() != null && !cv.getEmail().isBlank() && cv.getPhoneNumber() != null && !cv.getPhoneNumber().isBlank()) {
			strengths.add("Đã có thông tin liên hệ (email/phone) rõ ràng.");
		}
		if (cv.getYearsOfExperience() != null && cv.getYearsOfExperience() >= 3) {
			strengths.add("Kinh nghiệm tổng thể tốt (>= 3 năm).");
		} else {
			weaknesses.add("Kinh nghiệm tổng thể còn hạn chế.");
			advice.add("Làm nổi bật các dự án/công nghệ cốt lõi và kết quả định lượng.");
		}
		if (cv.getCoverLetter() == null || cv.getCoverLetter().isBlank()) {
			weaknesses.add("Thiếu cover letter thuyết phục.");
			advice.add("Bổ sung cover letter 150-250 từ nêu rõ thành tựu, động lực, và fit với JD.");
		}
		// Lời khuyên mặc định để tránh danh sách rỗng
		if (advice.isEmpty()) {
			advice.add("Cấu trúc CV rõ ràng: Tóm tắt, Kinh nghiệm, Kỹ năng, Dự án, Học vấn.");
			advice.add("Định lượng thành tựu bằng số liệu (%, doanh thu, thời gian, chi phí...).");
		}
		if (strengths.isEmpty()) {
			strengths.add("Thông tin cơ bản đã có sẵn, có thể bổ sung thành tựu để nổi bật hơn.");
		}

		return CVEvaluationResult.builder()
			.cvId(cv.getCvId())
			.seekerId(cv.getSeekerId())
			.overallScore(65)
			.strengths(strengths)
			.weaknesses(weaknesses)
			.improvementAdvice(advice)
			.extractedSkills(List.of())
			.targetRoles(null)
			.providerDetails(Map.of("note", "No AI provider configured; baseline heuristics used"))
			.build();
	}

	private CVEvaluationResult ensemble(CVUpload cv, List<AIProviderClient.AIResponse> responses) {
		// Trung bình điểm, gộp danh sách, unique và giới hạn kích thước
		int validScores = 0;
		int scoreSum = 0;
		for (AIProviderClient.AIResponse r : responses) {
			if (r.score != null) {
				validScores++;
				scoreSum += r.score;
			}
		}
		Integer finalScore = validScores == 0 ? null : Math.round((float) scoreSum / validScores);

		List<String> strengths = mergeUnique(responses.stream().map(r -> safeList(r.strengths)).collect(Collectors.toList()), 10);
		List<String> weaknesses = mergeUnique(responses.stream().map(r -> safeList(r.weaknesses)).collect(Collectors.toList()), 10);
		List<String> advice = mergeUnique(responses.stream().map(r -> safeList(r.advice)).collect(Collectors.toList()), 10);
		List<String> skills = mergeUnique(responses.stream().map(r -> safeList(r.skills)).collect(Collectors.toList()), 15);

		Map<String, Object> details = new LinkedHashMap<>();
		for (AIProviderClient.AIResponse r : responses) {
			details.put(r.providerName, Map.of(
				"model", r.modelName,
				"rawStatus", r.rawDetails != null ? r.rawDetails.getOrDefault("status", null) : null
			));
		}

		// chọn targetRoles đầu tiên có giá trị
		String targetRoles = responses.stream().map(r -> r.targetRoles).filter(Objects::nonNull).findFirst().orElse(null);

		return CVEvaluationResult.builder()
			.cvId(cv.getCvId())
			.seekerId(cv.getSeekerId())
			.overallScore(finalScore == null ? 70 : finalScore)
			.strengths(strengths)
			.weaknesses(weaknesses)
			.improvementAdvice(advice)
			.extractedSkills(skills)
			.targetRoles(targetRoles)
			.providerDetails(details)
			.build();
	}

	private List<String> safeList(List<String> input) {
		return input == null ? List.of() : input;
	}
	private List<String> mergeUnique(List<List<String>> lists, int limit) {
		LinkedHashSet<String> set = new LinkedHashSet<>();
		for (List<String> l : lists) {
			for (String s : l) {
				if (s != null && !s.isBlank()) {
					set.add(s.trim());
					if (set.size() >= limit) break;
				}
			}
			if (set.size() >= limit) break;
		}
		return new ArrayList<>(set);
	}
}


