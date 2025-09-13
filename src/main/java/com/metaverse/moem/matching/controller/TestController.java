package com.metaverse.moem.matching.controller;

import com.metaverse.moem.matching.service.EmbeddingPort;
import com.metaverse.moem.matching.service.OpenAIEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
    private final EmbeddingPort embeddingPort;

    @GetMapping("/embed")
    public List<Double> testEmbed(@RequestParam String text) {
        return embeddingPort.embed(text);
    }
}
