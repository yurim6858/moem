package com.metaverse.moem.matching.service;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GeminiService {

    private final String projectId;

    public GeminiService(@Value("${google.api.project.id}") String projectId) {
        this.projectId = projectId;
    }

    public String getCompletion(String prompt) throws IOException {
        try (VertexAI vertexAI = new VertexAI(projectId, "asia-northeast3")) {
            GenerativeModel model = new GenerativeModel("gemini-1.0-pro", vertexAI);
            GenerateContentResponse response = model.generateContent(prompt);
            return ResponseHandler.getText(response);
        }
    }
}