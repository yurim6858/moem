package com.metaverse.moem.matching.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
@Slf4j
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-pro}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String endpoint() {
        return "https://generativelanguage.googleapis.com/v1/models/"
                + model + ":generateContent?key=" + apiKey;
    }

    public String getCompletion(String prompt) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

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

        try {
            String res = restTemplate.postForObject(
                    endpoint(),
                    new HttpEntity<>(body, headers),
                    String.class
            );
            return parseGeminiResponse(res);
        } catch (HttpClientErrorException.NotFound nf) {
            log.warn("Model {} 404 → fallback to gemini-2.5-flash", model);
            String fallbackUrl =
                    "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + apiKey;
            String res = restTemplate.postForObject(
                    fallbackUrl,
                    new HttpEntity<>(body, headers),
                    String.class
            );
            return parseGeminiResponse(res);
        } catch (Exception error) {
            log.error("Gemini(API key) 호출 실패: {}", error.getMessage(), error);
            throw new IOException("Gemini API 호출에 실패했습니다.", error);
        }
    }

    private String parseGeminiResponse(String responseJson) throws IOException {
        JsonNode root = objectMapper.readTree(responseJson);
        return root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();
    }
}
