package com.metaverse.moem.matching.service;

import com.metaverse.moem.matching.domain.ProjectPost;

import java.util.List;

public interface ProjectMatchService {

    String getMatchReasonForUser(Long userId, Long projectId);

    List<ProjectPost> getRecommendedProjects(Long userId, int limit);
}