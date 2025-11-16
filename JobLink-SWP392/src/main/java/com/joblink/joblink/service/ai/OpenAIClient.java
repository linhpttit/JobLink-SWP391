package com.joblink.joblink.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class OpenAIClient implements AIProviderClient {

	@Value("${openai.api.key:}")
	private String apiKey;
	@Value("${openai.model:gpt-4o-mini}")
	private String model;
	@Value("${ai.openai.enabled:false}")
	private boolean enabled;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public boolean isEnabled() {
		return enabled && apiKey != null && !apiKey.isBlank();
	}

	@Override
	public AIResponse evaluateCVText(String prompt) {
		try {
			String body = """
				{
				  "model": "%s",
				  "temperature": 0.2,
				  "messages": [
				    {"role": "system", "content": "Bạn là chuyên gia tuyển dụng. Hãy trả về JSON với các field: score (0-100), strengths (string[]), weaknesses (string[]), advice (string[]), skills (string[]), targetRoles (string)."},
				    {"role": "user", "content": %s}
				  ],
				  "response_format": {"type":"json_object"}
				}
				""".formatted(model, objectMapper.writeValueAsString(prompt));

			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://api.openai.com/v1/chat/completions"))
				.header("Authorization", "Bearer " + apiKey)
				.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
				.timeout(Duration.ofSeconds(45))
				.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
				.build();

			HttpResponse<String> response = HttpClient.newHttpClient()
				.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

			AIResponse ai = new AIResponse();
			ai.providerName = "openai";
			ai.modelName = model;
			ai.rawText = response.body();

			if (response.statusCode() >= 200 && response.statusCode() < 300) {
				JsonNode root = objectMapper.readTree(response.body());
				String content = root.path("choices").path(0).path("message").path("content").asText();
				String jsonText = extractJson(content);
				JsonNode json = objectMapper.readTree(jsonText);
				ai.score = json.path("score").isInt() ? json.get("score").asInt() : null;
				ai.strengths = toList(json.get("strengths"));
				ai.weaknesses = toList(json.get("weaknesses"));
				ai.advice = toList(json.get("advice"));
				ai.skills = toList(json.get("skills"));
				ai.targetRoles = json.path("targetRoles").asText(null);
			}

			Map<String, Object> meta = new HashMap<>();
			meta.put("status", response.statusCode());
			ai.rawDetails = meta;
			return ai;
		} catch (Exception ex) {
			AIResponse ai = new AIResponse();
			ai.providerName = "openai";
			ai.modelName = model;
			ai.rawText = ex.getMessage();
			return ai;
		}
	}

	private List<String> toList(JsonNode node) {
		return node != null && node.isArray()
			? objectMapper.convertValue(node, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class))
			: List.of();
	}

	// Cố gắng lấy JSON thuần từ nội dung có thể chứa markdown/giải thích
	private String extractJson(String content) {
		if (content == null) return "{}";
		content = content.trim();
		// nếu đã là JSON
		if ((content.startsWith("{") && content.endsWith("}")) ||
			(content.startsWith("[") && content.endsWith("]"))) {
			return content;
		}
		// tìm trong code fence ```json ... ``` hoặc ``` ... ```
		Pattern fenceJson = Pattern.compile("```json\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
		Matcher m1 = fenceJson.matcher(content);
		if (m1.find()) {
			return m1.group(1).trim();
		}
		Pattern fence = Pattern.compile("```\\s*([\\s\\S]*?)```");
		Matcher m2 = fence.matcher(content);
		if (m2.find()) {
			String inner = m2.group(1).trim();
			if (inner.startsWith("{") || inner.startsWith("[")) return inner;
		}
		// fallback: lấy phần từ { đầu tiên đến } cuối cùng
		int s = content.indexOf('{');
		int e = content.lastIndexOf('}');
		if (s >= 0 && e > s) {
			return content.substring(s, e + 1);
		}
		return "{}";
	}
}


