package com.metaverse.moem.matching.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Slf4j
@Service
public class GeminiService {

    @Value("${google.api.project.id}")
    private String projectId;

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String geminiApiUrl = "https://aiplatform.googleapis.com/v1/projects/%s/locations/global/publishers/google/models/gemini-1.5-pro-latest:generateContent?key=%s";

    public String getCompletion(String prompt) throws IOException {
        String url = String.format(geminiApiUrl, projectId, apiKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String escapedPrompt = objectMapper.writeValueAsString(prompt);
        String requestBody = String.format("""
                {
                  "contents": [
                    {
                      "parts": [
                        { "text": %s }
                      ]
                    }
                  ]
                }
                """, escapedPrompt);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            String responseJson = restTemplate.postForObject(url, requestEntity, String.class);
            return parseGeminiResponse(responseJson);
        } catch (Exception e) {
            log.error("Gemini API 호출 실패: {}", e.getMessage());
            throw new IOException("Gemini API 호출에 실패했습니다.", e);
        }
    }


    private String parseGeminiResponse(String responseJson) throws IOException {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode textNode = root.path("candidates").get(0).path("content").path("parts").get(0).path("text");
            return textNode.asText();
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패. 응답 내용: {}", responseJson, e);
            throw new IOException("Gemini 응답을 파싱하는 데 실패했습니다.", e);
        }
    }
}