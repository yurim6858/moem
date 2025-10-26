package com.metaverse.moem.matching.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class GeminiService {

    @Value("${google.api.project.id}")
    private String projectId;

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String geminiApiUrl = "https://global-aiplatform.googleapis.com/v1/projects/%s/locations/global/publishers/google/models/gemini-1.5-pro-latest:generateContent?key=%s";

    public String getCompletion(String prompt) throws IOException {
        String url = String.format(geminiApiUrl, projectId, apiKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = String.format("""
                {
                  "contents": [
                    {
                      "parts": [
                        { "text": "%s" }
                      ]
                    }
                  ]
                }
                """, prompt);

        // 3. HTTP 요청 생성
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        // 4. API 호출 및 응답 받기
        String responseJson = restTemplate.postForObject(url, requestEntity, String.class);

        // 5. 복잡한 JSON 응답에서 텍스트만 추출
        return parseGeminiResponse(responseJson);
    }

    private String parseGeminiResponse(String responseJson) throws IOException {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode textNode = root.path("candidates").get(0).path("content").path("parts").get(0).path("text");
            return textNode.asText();
        } catch (Exception e) {
            System.err.println("Gemini 응답 파싱 실패: " + responseJson);
            throw new IOException("Gemini 응답을 파싱하는 데 실패했습니다.", e);
        }
    }
}