package com.metaverse.moem.matching.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@Profile("mock")
public class MockEmbeddingService implements EmbeddingPort{

    @Override
    public List<Double> embed(String text) {

        long seed = text == null ? 0 : text.hashCode();
        Random random = new Random(seed);
        return Arrays.asList(
                random.nextDouble(),
                random.nextDouble(),
                random.nextDouble()
        );
    }
}
