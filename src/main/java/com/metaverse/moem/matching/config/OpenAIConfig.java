package com.metaverse.moem.matching.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

    @Value("${openai.api-key:${OPENAI_API_KEY:}}")
    private String apiKey;

    @Bean
    public OpenAIClient openAI() {
        System.out.println("OpenAI API Key: " + (apiKey != null && !apiKey.isBlank()));
        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }
}
