package com.metaverse.moem.team.service;

import com.metaverse.moem.team.domain.Team;
import com.metaverse.moem.team.domain.TeamMembers;
import com.metaverse.moem.matching.domain.UserPost;
import com.metaverse.moem.team.dto.TeamMembersDto;
import com.metaverse.moem.team.repository.TeamMembersRepository;
import com.metaverse.moem.team.repository.TeamRepository;
import com.metaverse.moem.matching.repository.UserPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamMembersService {

    private final TeamMembersRepository teamMembersRepository;
    private final TeamRepository teamRepository;
    private final UserPostRepository userPostRepository;

    // 팀원 생성
    public TeamMembersDto.Res create(Long teamId, TeamMembersDto.CreateReq req) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팀이 존재하지 않습니다."));

        UserPost user = userPostRepository.findById(req.userId())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        TeamMembers member = TeamMembers.builder()
                .team(team)
                .userId(req.userId())
                .role(req.role())
                .status("Active")
                .joinAt(LocalDateTime.now())
                .build();

        TeamMembers saved = teamMembersRepository.save(member);

        return new TeamMembersDto.Res(
                saved.getId(),
                user.getUsername(),
                saved.getRole(),
                saved.getTeam().getId(),
                saved.getJoinAt().toString(),
                saved.getJoinAt().toString()
        );
    }

    // 팀원 수정
    public TeamMembersDto.Res update(Long memberId, TeamMembersDto.UpdateReq req) {
        TeamMembers member = teamMembersRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팀원이 존재하지 않습니다."));

        if (req.role() != null) {
            member.updateRole(req.role());
        }

        if (req.name() != null) {
            member.updateName(req.name());
        }


        UserPost user = userPostRepository.findById(member.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        return new TeamMembersDto.Res(
                member.getId(),
                member.getName(),
                member.getRole(),
                member.getTeam().getId(),
                member.getJoinAt().toString(),
                LocalDateTime.now().toString()
        );
    }

    // 팀원 삭제
    public void delete(TeamMembersDto.DeleteReq req) {
        TeamMembers member = teamMembersRepository.findById(req.id())
                .orElseThrow(() -> new IllegalArgumentException("해당 팀원이 존재하지 않습니다."));

        teamMembersRepository.delete(member);
    }

    // 팀원 조회
    public List<TeamMembersDto.Res> getMembersByTeamId(Long teamId) {

        List<TeamMembers> members = teamMembersRepository.findByTeamId(teamId);

        return members.stream()
                .map(member -> {
                    UserPost user = userPostRepository.findById(member.getUserId())
                            .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

                    return new TeamMembersDto.Res(
                            member.getId(),
                            user.getUsername(),
                            member.getRole(),
                            member.getTeam().getId(),
                            member.getJoinAt().toString(),
                            LocalDateTime.now().toString()
                    );
                })
                .toList();
    }
}
