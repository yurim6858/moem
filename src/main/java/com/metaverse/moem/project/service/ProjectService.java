package com.metaverse.moem.project.service;

import com.metaverse.moem.project.domain.Project;
import com.metaverse.moem.project.dto.ProjectDto;
import com.metaverse.moem.project.repository.ProjectRepository;
import com.metaverse.moem.team.domain.Team;
import com.metaverse.moem.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;

    @Transactional
    public ProjectDto.Res create(ProjectDto.CreateReq req) {
        Project project = new Project();
        project.setName(req.name());
        project.setDescription(req.description());
        project.setType(req.type());
        project.setRecruitTotal(req.recruitTotal());
        project.setRecruitCurrent(0);
        project.setRecruitStartDate(req.recruitStartDate());
        project.setRecruitEndDate(req.recruitEndDate());
        project.setProjectStartDate(req.projectStartDate());
        project.setProjectEndDate(req.projectEndDate());

        projectRepository.save(project);


        Team team = Team.create(project, req.teamName(), null);
        teamRepository.save(team);

        project.setTeam(team);

        return ProjectDto.Res.from(project);
    }

    @Transactional(readOnly = true)
    public Optional<ProjectDto.Res> get(Long id) {
        return projectRepository.findByIdAndIsDeletedFalse(id).map(ProjectDto.Res::from);
    }

    @Transactional(readOnly = true)
    public List<ProjectDto.Res> listPublic(ProjectDto.SearchCondition condition) {
        return projectRepository.searchPublic(condition.type(), condition.status(), condition.query())
                .stream().map(ProjectDto.Res::from).toList();
    }

    @Transactional
    public Optional<ProjectDto.Res> update(Long id, ProjectDto.UpdateReq req) {
        return projectRepository.findByIdAndIsDeletedFalse(id)
                .map(project -> {
                    project.setName(req.name());
                    project.setDescription(req.description());
                    project.setType(req.type());
                    project.setRecruitTotal(req.recruitTotal());
                    project.setRecruitStartDate(req.recruitStartDate());
                    project.setRecruitEndDate(req.recruitEndDate());
                    project.setProjectStartDate(req.projectStartDate());
                    project.setProjectEndDate(req.projectEndDate());
                    return ProjectDto.Res.from(project);
                });
    }

    @Transactional
    public boolean delete(Long id) {
        return projectRepository.findByIdAndIsDeletedFalse(id)
                .map(project -> {
                    project.setDeleted(true);
                    return true;
                })
                .orElse(false);
    }
}
