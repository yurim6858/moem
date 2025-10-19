package com.metaverse.moem.team.service;

import com.metaverse.moem.team.domain.Team;
import com.metaverse.moem.team.dto.TeamDto;
import com.metaverse.moem.team.repository.TeamRepository;
import com.metaverse.moem.team.repository.TeamMembersRepository;
import com.metaverse.moem.auth.domain.User;
import com.metaverse.moem.auth.repository.UserRepository;
import com.metaverse.moem.matching.domain.ProjectPost;
import com.metaverse.moem.matching.repository.ProjectPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final UserRepository userRepository;
    private final ProjectPostRepository projectPostRepository;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TeamService(TeamRepository teamRepository, 
                      TeamMembersRepository teamMembersRepository,
                      UserRepository userRepository,
                      ProjectPostRepository projectPostRepository) {
        this.teamRepository = teamRepository;
        this.teamMembersRepository = teamMembersRepository;
        this.userRepository = userRepository;
        this.projectPostRepository = projectPostRepository;
    }

    // 팀 생성
    public TeamDto.Res create(TeamDto.CreateReq req) {
        if (teamRepository.existsByName(req.name())) {
            throw new IllegalArgumentException("이미 존재하는 팀 이름입니다.");
        }
        Team saved = teamRepository.save(
                Team.builder()
                        .name(req.name())
                        .description(req.description())
                        .build()
        );
        return toRes(saved);
    }

    // 팀 수정
    public TeamDto.Res update(Long id, TeamDto.UpdateReq req) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        // 값 변경
        team.setName(req.name());
        team.setDescription(req.description());

        // 저장 후 응답 반환
        Team updated = teamRepository.save(team);
        return toRes(updated);
    }

    // 팀 삭제
    public void delete(Long id) {
        if (!teamRepository.existsById(id)) {
            throw new IllegalArgumentException("팀을 찾을 수 없습니다.");
        }
        teamRepository.deleteById(id);
    }

    // 팀 목록 조회
    @Transactional(readOnly = true)
    public List<TeamDto.Res> list() {
        return teamRepository.findAll()
                .stream()
                .map(this::toRes)      // → TeamDto.Res 로 매핑
                .toList();
    }

    // 내가 속한 팀 목록 조회
    @Transactional(readOnly = true)
    public List<TeamDto.Res> getMyTeams(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 사용자가 속한 팀들의 ID 조회
        List<Long> teamIds = teamMembersRepository.findByUser(user)
                .stream()
                .map(member -> member.getTeam().getId())
                .toList();

        if (teamIds.isEmpty()) {
            return List.of();
        }

        // 팀 정보 조회
        return teamRepository.findAllById(teamIds)
                .stream()
                .map(this::toRes)
                .toList();
    }

    // 팀 상세 정보 조회 (멤버 포함)
    @Transactional(readOnly = true)
    public TeamDto.DetailRes getTeamInfo(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        // 팀 멤버 조회
        List<com.metaverse.moem.team.dto.TeamMembersDto.Res> members = 
                teamMembersRepository.findByTeamId(teamId)
                        .stream()
                        .map(member -> {
                            return new com.metaverse.moem.team.dto.TeamMembersDto.Res(
                                    member.getId(),
                                    member.getName(),
                                    member.getRole(),
                                    member.getTeam().getId(),
                                    member.getUserId(),
                                    member.getJoinAt().toString(),
                                    member.getJoinAt().toString()
                            );
                        })
                        .toList();

        // 프로젝트 ID 조회 (팀과 연결된 프로젝트)
        Long projectId = null;
        try {
            Optional<ProjectPost> projectOpt = projectPostRepository.findByTeam_Id(teamId);
            if (projectOpt.isPresent()) {
                projectId = projectOpt.get().getId();
            }
        } catch (Exception e) {
            // 프로젝트가 없어도 팀 정보는 반환
            System.out.println("팀과 연결된 프로젝트를 찾을 수 없습니다: " + e.getMessage());
        }

        return new TeamDto.DetailRes(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getCreatedAt() != null ? team.getCreatedAt().format(FMT) : null,
                team.getUpdatedAt() != null ? team.getUpdatedAt().format(FMT) : null,
                members,
                members.size(),
                projectId
        );
    }

    // Entity -> 응답 DTO 변환기
    private TeamDto.Res toRes(Team team) {   // ✅ 반환 타입 수정
        return new TeamDto.Res(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getCreatedAt() != null ? team.getCreatedAt().format(FMT) : null,
                team.getUpdatedAt() != null ? team.getUpdatedAt().format(FMT) : null
        );
    }

    // 프로젝트 시작 준비 상태 확인
    @Transactional(readOnly = true)
    public TeamDto.StartReadyRes checkStartReady(Long teamId, Long projectId) {
        // 팀 정보 조회 (존재 여부 확인)
        teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        // 프로젝트 정보 조회
        ProjectPost project = projectPostRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));

        // 팀 멤버 조회
        List<com.metaverse.moem.team.domain.TeamMembers> members = 
                teamMembersRepository.findByTeamId(teamId);

        // 포지션 상태 분석
        List<TeamDto.PositionStatus> positionStatuses = analyzePositionStatus(project.getPositions(), members);
        
        // 전체 필요 포지션 수
        int totalRequired = project.getPositions().stream()
                .mapToInt(pos -> pos.getHeadcount() != null ? pos.getHeadcount() : 0)
                .sum();
        
        // 채워진 포지션 수
        int filledPositions = positionStatuses.stream()
                .mapToInt(status -> status.isFilled() ? status.required() : 0)
                .sum();
        
        // 완성률 계산
        double completionRate = totalRequired > 0 ? (double) filledPositions / totalRequired * 100 : 0;
        
        // 시작 가능 여부
        boolean isReadyToStart = positionStatuses.stream().allMatch(TeamDto.PositionStatus::isFilled);
        
        // 메시지 생성
        String message = isReadyToStart 
                ? "🎉 모든 포지션이 채워졌습니다! 프로젝트를 시작할 수 있습니다."
                : String.format("📊 진행률: %.1f%% (%d/%d 포지션)", completionRate, filledPositions, totalRequired);

        return new TeamDto.StartReadyRes(
                isReadyToStart,
                totalRequired,
                filledPositions,
                completionRate,
                positionStatuses,
                message
        );
    }

    // 포지션 상태 분석
    private List<TeamDto.PositionStatus> analyzePositionStatus(
            List<ProjectPost.Position> requiredPositions, 
            List<com.metaverse.moem.team.domain.TeamMembers> members) {
        
        return requiredPositions.stream()
                .map(position -> {
                    String role = position.getRole();
                    int required = position.getHeadcount() != null ? position.getHeadcount() : 0;
                    
                    // 해당 역할의 현재 멤버 수 계산
                    int current = (int) members.stream()
                            .filter(member -> role.equals(member.getRole()))
                            .count();
                    
                    boolean isFilled = current >= required;
                    
                    return new TeamDto.PositionStatus(role, required, current, isFilled);
                })
                .toList();
    }

    // 프로젝트 시작
    @Transactional
    public void startProject(Long teamId, Long projectId) {
        // 팀 정보 조회
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        // 프로젝트 정보 조회
        ProjectPost project = projectPostRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));

        // 시작 준비 상태 확인
        TeamDto.StartReadyRes startReady = checkStartReady(teamId, projectId);
        if (!startReady.isReadyToStart()) {
            throw new IllegalArgumentException("프로젝트 시작 조건을 만족하지 않습니다: " + startReady.message());
        }

        // TODO: 프로젝트 상태를 "진행중"으로 변경하는 로직 추가
        // TODO: 팀 상태를 "활성"으로 변경하는 로직 추가
        // TODO: 프로젝트 시작일 기록
        // TODO: 팀원들에게 알림 발송

        // 현재는 성공 메시지만 반환
        System.out.println("프로젝트가 성공적으로 시작되었습니다! 팀: " + team.getName() + ", 프로젝트: " + project.getTitle());
    }
}
