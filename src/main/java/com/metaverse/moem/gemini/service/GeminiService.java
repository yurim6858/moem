package com.metaverse.moem.gemini.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
@Slf4j
@ConditionalOnExpression("!'${gemini.api.key:}'.isEmpty()")
public class GeminiService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-pro}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();


    public String getCompletion(String prompt) throws IOException {
        try {
            return callGeminiApi(prompt, model);
        } catch (HttpClientErrorException.NotFound nf) {
            log.warn("Gemini 모델 {} 호출 실패(404), gemini-2.5-flash로 대체합니다.", model);
            return callGeminiApi(prompt, "gemini-2.5-flash");
        } catch (Exception error) {
            log.error("Gemini API 호출 실패: {}", error.getMessage(), error);
            throw new IOException("Gemini API 호출에 실패했습니다.", error);
        }
    }

    public String generateContent(String systemPrompt, String userPrompt) throws IOException {
        String fullPrompt = systemPrompt + "\n\n" + userPrompt;
        try {
            return getCompletion(fullPrompt);
        } catch (Exception error) {
            log.error("Gemini API (with System Prompt) 호출 실패: {}", error.getMessage(), error);
            throw new IOException("Gemini API 호출에 실패했습니다.", error);
        }
    }

    private String callGeminiApi(String prompt, String targetModel) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String apiUrl = endpoint(targetModel);

        String body = """
        {
          "contents": [
            {
              "role": "user",
              "parts": [ { "text": %s } ]
            }
          ]
        }
        """.formatted(objectMapper.writeValueAsString(prompt));

        String res = restTemplate.postForObject(
                apiUrl,
                new HttpEntity<>(body, headers),
                String.class
        );
        return parseGeminiResponse(res);
    }

    private String endpoint(String modelName) {
        return "https://generativelanguage.googleapis.com/v1/models/"
                + modelName + ":generateContent?key=" + apiKey;
    }

    private String parseGeminiResponse(String responseJson) throws IOException {
        JsonNode root = objectMapper.readTree(responseJson);

        JsonNode candidates = root.path("candidates");
        if (candidates.isEmpty() || candidates.get(0).path("content").path("parts").isEmpty()) {
            log.error("Gemini API 응답 실패: {}", responseJson);
            throw new IOException("Gemini API에서 유효한 텍스트 응답을 받지 못했습니다.");
        }

        return candidates.get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();
    }
}