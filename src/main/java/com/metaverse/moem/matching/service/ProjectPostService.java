package com.metaverse.moem.matching.service;

import com.metaverse.moem.matching.domain.ProjectPost;
import com.metaverse.moem.auth.domain.User;
import com.metaverse.moem.auth.repository.UserRepository;
import com.metaverse.moem.matching.dto.MatchingRequest;
import com.metaverse.moem.matching.dto.MatchingResponse;
import com.metaverse.moem.matching.repository.ProjectPostRepository;
import com.metaverse.moem.team.domain.Team;
import com.metaverse.moem.team.domain.TeamMembers;
import com.metaverse.moem.team.repository.TeamRepository;
import com.metaverse.moem.team.repository.TeamMembersRepository;
import com.metaverse.moem.project.domain.Project;
import com.metaverse.moem.project.repository.ProjectRepository;
import com.metaverse.moem.project.domain.ProjectType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProjectPostService {

    private final ProjectPostRepository projectPostRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final ProjectRepository projectRepository;

    public ProjectPostService(ProjectPostRepository projectPostRepository, 
                             UserRepository userRepository,
                             TeamRepository teamRepository,
                             TeamMembersRepository teamMembersRepository,
                             ProjectRepository projectRepository) {
        this.projectPostRepository = projectPostRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.teamMembersRepository = teamMembersRepository;
        this.projectRepository = projectRepository;
    }

    public MatchingResponse create(MatchingRequest req) {
        try {
            System.out.println("=== ProjectPostService: 프로젝트 생성 시작 ===");
            System.out.println("ProjectPostService: creatorId: " + req.getCreatorId());
            System.out.println("ProjectPostService: username: " + req.getUsername());
            System.out.println("ProjectPostService: title: " + req.getTitle());
            System.out.println("ProjectPostService: deadline: " + req.getDeadline());
            
            // creatorId 유효성 검사
            if (req.getCreatorId() == null) {
                System.err.println("ProjectPostService: ERROR - creatorId가 null입니다!");
                throw new RuntimeException("작성자 ID가 필요합니다.");
            }
            
            // 작성자 조회 (username으로 조회)
            User creator = userRepository.findByUsername(req.getUsername())
                        .orElseThrow(() -> new RuntimeException("작성자를 찾을 수 없습니다. Username: " + req.getUsername()));
            
            System.out.println("ProjectPostService: 작성자 조회 성공: " + creator.getUsername());

        ProjectPost projectPost = new ProjectPost();
        projectPost.setTitle(safeTrim(req.getTitle()));
        projectPost.setIntro(safeTrim(req.getIntro()));
        projectPost.setDescription(safeTrim(req.getDescription()));
        projectPost.setTags(req.getTags() == null ? new ArrayList<>() : req.getTags());
        projectPost.setDeadline(req.getDeadline().atStartOfDay());
        projectPost.setCreator(creator);
        projectPost.setWorkStyle(safeTrim(req.getWorkStyle()));
        projectPost.setContactType(safeTrim(req.getContactType()));
        projectPost.setContactValue(safeTrim(req.getContactValue()));
        projectPost.setCollaborationPeriod(safeTrim(req.getCollaborationPeriod()));
        
        // 포지션 변환
        if (req.getPositions() != null) {
            List<ProjectPost.Position> positions = req.getPositions().stream()
                    .map(pos -> {
                        ProjectPost.Position position = new ProjectPost.Position();
                        position.setRole(safeTrim(pos.getRole()));
                        position.setHeadcount(pos.getHeadcount());
                        return position;
                    })
                    .filter(pos -> pos.getRole() != null && !pos.getRole().isEmpty())
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            projectPost.setPositions(positions);
        }
        
            ProjectPost saved = projectPostRepository.save(projectPost);
            System.out.println("ProjectPostService: 프로젝트 저장 완료 - ID: " + saved.getId());
            
            // 프로젝트 생성과 동시에 팀 생성 및 팀장 추가
            createTeamForProject(saved, creator);
            
            System.out.println("ProjectPostService: 프로젝트 생성 완료");
            return toResp(saved);
        } catch (Exception e) {
            System.err.println("ProjectPostService: 프로젝트 생성 실패 - " + e.getMessage());
            e.printStackTrace();
            
            // 더 구체적인 에러 메시지 제공
            if (e.getMessage() != null) {
                if (e.getMessage().contains("작성자를 찾을 수 없습니다")) {
                    throw new RuntimeException("사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요.", e);
                } else if (e.getMessage().contains("팀 생성 중 오류")) {
                    throw new RuntimeException("팀 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", e);
                } else if (e.getMessage().contains("ConstraintViolationException") || e.getMessage().contains("DataIntegrityViolationException")) {
                    throw new RuntimeException("데이터 저장 중 오류가 발생했습니다. 입력 정보를 확인해주세요.", e);
                }
            }
            
            throw new RuntimeException("프로젝트 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<MatchingResponse> list() {
        return projectPostRepository.findAll().stream().map(this::toResp).toList();
    }

    @Transactional(readOnly = true)
    public MatchingResponse get(Long id) {
        ProjectPost projectPost = projectPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND"));
        return toResp(projectPost);
    }

    @Transactional
    public MatchingResponse update(Long id, MatchingRequest req) {
        ProjectPost projectPost = projectPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND"));
        
        projectPost.setTitle(safeTrim(req.getTitle()));
        projectPost.setIntro(safeTrim(req.getIntro()));
        projectPost.setDescription(safeTrim(req.getDescription()));
        projectPost.setTags(req.getTags() == null ? new ArrayList<>() : req.getTags());
        projectPost.setDeadline(req.getDeadline().atStartOfDay());
        projectPost.setWorkStyle(safeTrim(req.getWorkStyle()));
        projectPost.setContactType(safeTrim(req.getContactType()));
        projectPost.setContactValue(safeTrim(req.getContactValue()));
        projectPost.setCollaborationPeriod(safeTrim(req.getCollaborationPeriod()));
        
        // 포지션 변환
        if (req.getPositions() != null) {
            List<ProjectPost.Position> positions = req.getPositions().stream()
                    .map(pos -> {
                        ProjectPost.Position position = new ProjectPost.Position();
                        position.setRole(safeTrim(pos.getRole()));
                        position.setHeadcount(pos.getHeadcount());
                        return position;
                    })
                    .filter(pos -> pos.getRole() != null && !pos.getRole().isEmpty())
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            projectPost.setPositions(positions);
        }
        
        ProjectPost saved = projectPostRepository.save(projectPost);
        
        // 프로젝트와 연결된 팀 정보도 동기화
        if (saved.getTeam() != null) {
            System.out.println("=== 프로젝트 수정 시 팀 동기화 시작 ===");
            System.out.println("프로젝트 ID: " + saved.getId());
            System.out.println("팀 ID: " + saved.getTeam().getId());
            System.out.println("프로젝트 제목: " + saved.getTitle());
            System.out.println("프로젝트 소개: " + saved.getIntro());
            updateTeamFromProject(saved);
        } else {
            System.out.println("프로젝트에 연결된 팀이 없습니다: " + saved.getId());
        }
        
        return toResp(saved);
    }

    private MatchingResponse toResp(ProjectPost projectPost) {
        List<MatchingResponse.PositionResponse> positions = projectPost.getPositions() == null ? 
                new ArrayList<>() : 
                projectPost.getPositions().stream()
                        .map(pos -> new MatchingResponse.PositionResponse(pos.getRole(), pos.getHeadcount()))
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        
        return new MatchingResponse(
                projectPost.getId(),
                projectPost.getTitle(),
                projectPost.getIntro(),
                projectPost.getDescription(),
                projectPost.getTags(),
                projectPost.getDeadline().toLocalDate(),
                projectPost.getCreatorUsername(),
                projectPost.getWorkStyle(),
                projectPost.getContactType(),
                projectPost.getContactValue(),
                projectPost.getCollaborationPeriod(),
                positions
        );
    }

    private String safeTrim(String s) {
        return s == null ? null : s.trim();
    }

    public void delete(Long id) {
        if (!projectPostRepository.existsById(id)) {
            throw new IllegalArgumentException("NOT_FOUND");
        }
        projectPostRepository.deleteById(id);
    }

    // 프로젝트 생성 시 팀 생성 및 팀장 추가
    private void createTeamForProject(ProjectPost project, User creator) {
        try {
            System.out.println("ProjectPostService: 팀 생성 시작 - 프로젝트: " + project.getTitle());
            System.out.println("ProjectPostService: 팀장 정보 - ID: " + creator.getId() + ", 이름: " + creator.getUsername());
            
            // 고유한 팀 이름 생성
            String baseTeamName = project.getTitle() + " 팀";
            String teamName = baseTeamName;
            int counter = 1;
            
            // 팀 이름이 이미 존재하는 경우 번호를 추가하여 고유성 보장
            while (teamRepository.existsByName(teamName)) {
                teamName = baseTeamName + " (" + counter + ")";
                counter++;
            }
            
            System.out.println("ProjectPostService: 최종 팀 이름: " + teamName);
            
            // Project 엔티티 생성 (Team 생성에 필요)
            Project projectEntity = new Project();
            projectEntity.setName(project.getTitle());
            projectEntity.setDescription(project.getIntro());
            projectEntity.setOwnerId(creator.getId());
            projectEntity.setType(ProjectType.DEVELOPMENT); // 기본값으로 DEVELOPMENT 사용
            projectEntity.setRecruitCurrent(0);
            projectEntity.setRecruitTotal(project.getPositions().stream()
                    .mapToInt(pos -> pos.getHeadcount() != null ? pos.getHeadcount() : 0)
                    .sum());
            // isDeleted는 기본값 false로 초기화됨
            
            // Project 저장
            Project savedProject = projectRepository.save(projectEntity);
            
            // 팀 생성 (maxMembers는 null로 설정하면 기본값 0으로 설정됨)
            Team team = Team.create(savedProject, teamName, null);
            
            System.out.println("ProjectPostService: 팀 엔티티 생성 완료");
            Team savedTeam = teamRepository.save(team);
            System.out.println("ProjectPostService: 팀 저장 완료 - ID: " + savedTeam.getId());
            
            // 프로젝트에 팀 연결
            project.setTeam(savedTeam);
            projectPostRepository.save(project);
            System.out.println("ProjectPostService: 프로젝트에 팀 연결 완료");
            
            // 프로젝트 작성자를 팀장으로 추가
            TeamMembers teamLeader = TeamMembers.create(
                    savedTeam, 
                    creator.getId(), 
                    com.metaverse.moem.team.domain.Role.MANAGER
            );
            
            System.out.println("ProjectPostService: 팀 멤버 엔티티 생성 완료");
            teamMembersRepository.save(teamLeader);
            System.out.println("ProjectPostService: 팀 멤버 저장 완료");
            
            System.out.println("팀 생성 성공: " + savedTeam.getName() + ", 팀장: " + creator.getUsername());
        } catch (Exception e) {
            System.err.println("팀 생성 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("팀 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 프로젝트 정보로 팀 정보 동기화
    private void updateTeamFromProject(ProjectPost project) {
        try {
            Team team = project.getTeam();
            if (team == null) {
                System.out.println("팀이 null입니다.");
                return;
            }
            
            System.out.println("팀 동기화 시작 - 팀명: " + team.getName());
            System.out.println("새 프로젝트 소개: " + project.getIntro());
            
            boolean updated = false;
            
            // 팀 이름을 프로젝트 제목으로 업데이트
            if (project.getTitle() != null && !project.getTitle().trim().isEmpty()) {
                String newName = project.getTitle();
                if (!newName.equals(team.getName())) {
                    System.out.println("팀 이름 업데이트: " + team.getName() + " -> " + newName);
                    team.updateInfo(newName, team.getMaxMembers());
                    updated = true;
                } else {
                    System.out.println("팀 이름이 동일하여 업데이트하지 않음");
                }
            }
            
            if (updated) {
                teamRepository.save(team);
                System.out.println("팀 정보 동기화 완료: " + team.getName() + " - 이름 업데이트");
            } else {
                System.out.println("업데이트할 내용이 없음");
            }
        } catch (Exception e) {
            System.err.println("팀 정보 동기화 실패: " + e.getMessage());
            e.printStackTrace();
            // 동기화 실패해도 프로젝트 업데이트는 성공으로 처리
        }
    }
}
