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
public class GeminiClient implements AIProviderClient {

	@Value("${gemini.api.key:}")
	private String apiKey;
	@Value("${gemini.model:gemini-1.5-flash}")
	private String model;
	@Value("${ai.gemini.enabled:true}")
	private boolean enabled;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public boolean isEnabled() {
		return enabled && apiKey != null && !apiKey.isBlank();
	}

	@Override
	public AIResponse evaluateCVText(String prompt) {
		try {
			// Gemini generateContent API
			Map<String, Object> payload = Map.of(
				"contents", List.of(Map.of(
					"parts", List.of(Map.of("text",
						"Bạn là chuyên gia tuyển dụng. Hãy trả về JSON với các field: score (0-100), strengths (string[]), weaknesses (string[]), advice (string[]), skills (string[]), targetRoles (string).\n\nDỮ LIỆU:\n" + prompt
					))
				))
			);
			String body = objectMapper.writeValueAsString(payload);

			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey))
				.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
				.timeout(Duration.ofSeconds(45))
				.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
				.build();

			HttpResponse<String> response = HttpClient.newHttpClient()
				.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

			AIResponse ai = new AIResponse();
			ai.providerName = "gemini";
			ai.modelName = model;
			ai.rawText = response.body();

			if (response.statusCode() >= 200 && response.statusCode() < 300) {
				JsonNode root = objectMapper.readTree(response.body());
				// Gemini trả text ở candidates[0].content.parts[0].text
				String content = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
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
			ai.providerName = "gemini";
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

	private String extractJson(String content) {
		if (content == null) return "{}";
		content = content.trim();
		if ((content.startsWith("{") && content.endsWith("}")) ||
			(content.startsWith("[") && content.endsWith("]"))) {
			return content;
		}
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
		int s = content.indexOf('{');
		int e = content.lastIndexOf('}');
		if (s >= 0 && e > s) {
			return content.substring(s, e + 1);
		}
		return "{}";
	}
}


