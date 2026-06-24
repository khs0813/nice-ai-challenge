package com.example.batchmonitor.service;

import com.example.batchmonitor.config.OpenAiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    private final OpenAiProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OpenAiService(OpenAiProperties properties,
                         RestTemplateBuilder restTemplateBuilder,
                         ObjectMapper objectMapper) {
        this.properties = properties;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .setReadTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .build();
        this.objectMapper = objectMapper;
    }

    public boolean isConfigured() {
        return properties.isConfigured();
    }

    public String generate(String developerInstructions, String userInput) {
        if (!isConfigured()) {
            throw new IllegalStateException("OpenAI API 설정이 필요합니다. OPENAI_API_KEY를 설정하세요.");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(properties.getApiKey());

            Map<String, Object> body = new LinkedHashMap<String, Object>();
            body.put("model", properties.getModel());
            body.put("max_output_tokens", properties.getMaxOutputTokens());

            List<Map<String, String>> input = new ArrayList<Map<String, String>>();
            input.add(message("developer", developerInstructions));
            input.add(message("user", userInput));
            body.put("input", input);

            ResponseEntity<String> response = restTemplate.exchange(
                    properties.getBaseUrl() + "/responses",
                    HttpMethod.POST,
                    new HttpEntity<Map<String, Object>>(body, headers),
                    String.class
            );
            return extractOutputText(response.getBody());
        } catch (RestClientException e) {
            throw new IllegalStateException("OpenAI API 호출에 실패했습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IllegalStateException("OpenAI API 응답 처리에 실패했습니다: " + e.getMessage(), e);
        }
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> message = new LinkedHashMap<String, String>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String extractOutputText(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        JsonNode outputText = root.get("output_text");
        if (outputText != null && outputText.isTextual()) {
            return outputText.asText();
        }
        StringBuilder builder = new StringBuilder();
        JsonNode output = root.get("output");
        if (output != null && output.isArray()) {
            for (JsonNode item : output) {
                JsonNode content = item.get("content");
                if (content == null || !content.isArray()) {
                    continue;
                }
                for (JsonNode part : content) {
                    JsonNode text = part.get("text");
                    if (text != null && text.isTextual()) {
                        if (builder.length() > 0) {
                            builder.append('\n');
                        }
                        builder.append(text.asText());
                    }
                }
            }
        }
        if (builder.length() == 0) {
            throw new IllegalStateException("응답에서 output_text를 찾을 수 없습니다.");
        }
        return builder.toString();
    }
}
