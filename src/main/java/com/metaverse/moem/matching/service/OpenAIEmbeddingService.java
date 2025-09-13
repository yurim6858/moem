package com.metaverse.moem.matching.service;

import com.openai.client.OpenAIClient;
import com.openai.errors.OpenAIException;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.EmbeddingCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile("real")
@RequiredArgsConstructor
public class OpenAIEmbeddingService {

    private final OpenAIClient openAI;

    public List<Double> embed(String text) {
        try {
            EmbeddingCreateParams params = EmbeddingCreateParams.builder()
                    .model("text-embedding-3-small")
                    .encodingFormat(EmbeddingCreateParams.EncodingFormat.FLOAT)
                    .input(text)
                    .build();

            CreateEmbeddingResponse response = openAI.embeddings().create(params);

            List<Float> floats = response.data().get(0).embedding();
            return floats.stream().map(Float::doubleValue).collect(Collectors.toList());

        } catch (OpenAIException openAIException) {
            System.err.println("=== OpenAIException ===");
            System.err.println("message   : " + openAIException.getMessage());
            System.err.println("class     : " + openAIException.getClass().getName());
            Throwable cause = openAIException.getCause();
            if (cause != null) {
                System.err.println("cause     : " + cause.getClass().getName() + " - " + cause.getMessage());
            }

            openAIException.printStackTrace();
            throw openAIException;
        } catch (RuntimeException exception) {
            System.err.println("=== RuntimeException ===");
            exception.printStackTrace();
            throw exception;
        }
    }
}
