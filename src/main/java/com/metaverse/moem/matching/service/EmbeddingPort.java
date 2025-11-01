package com.metaverse.moem.matching.service;

import java.util.List;

public interface EmbeddingPort {
    List<Double> embed(String text);
}
