# 엔티티 관계 검토 보고서

## 1. 엔티티 관계 구조

### 1.1 ProjectPost (공고)
- **User (creator)**: `@ManyToOne` - 작성자
- **Team**: `@OneToOne` - 팀 (nullable = true)

### 1.2 Team (팀)
- **Project**: `@OneToOne` - 프로젝트 (nullable = false, unique = true)
- **TeamMembers**: `@OneToMany` - 팀원들 (CascadeType.ALL, orphanRemoval = true)
- **Meeting**: `@OneToMany` - 회의들 (CascadeType.ALL, orphanRemoval = true)
- **ProjectPost**: 양방향 관계 없음 (단방향만)

### 1.3 Project (프로젝트)
- **Team**: `@OneToOne(mappedBy="project")` - 팀
- **User (owner)**: 연관관계 없음 (ownerId만 Long)

### 1.4 Application (지원서)
- **ProjectPost**: `@ManyToOne` - 공고
- **User (applicant)**: `@ManyToOne` - 지원자

### 1.5 TeamMembers (팀원)
- **Team**: `@ManyToOne` - 팀
- **User**: 연관관계 없음 (userId만 Long)

### 1.6 TeamMemberLeaveRequest (팀원 나가기 요청)
- **Team**: `@ManyToOne` - 팀
- **TeamMembers**: `@ManyToOne` - 팀원

## 2. 발견된 문제점

### 2.1 ⚠️ TeamMemberLeaveRequest와 TeamMembers 관계
**문제**: TeamMembers가 삭제되면 TeamMemberLeaveRequest의 외래키가 무효화됨
- TeamMemberLeaveRequest는 TeamMembers를 참조하지만, TeamMembers 삭제 시 처리 로직 없음
- 현재: TeamMembers 삭제 시 TeamMemberLeaveRequest는 고아 레코드가 될 수 있음

**해결 방안**:
1. TeamMembers 삭제 시 관련된 TeamMemberLeaveRequest도 함께 삭제
2. 또는 TeamMemberLeaveRequest에 `@OnDelete(action = OnDeleteAction.CASCADE)` 추가

### 2.2 ⚠️ ProjectPost 삭제 시 Application 처리
**문제**: ProjectPost 삭제 시 Application이 고아 레코드가 될 수 있음
- 현재: ProjectPost 삭제 시 Application 처리 로직 없음
- Application은 ProjectPost를 필수로 참조 (`nullable = false`)

**해결 방안**:
1. ProjectPost 삭제 전에 관련 Application 삭제 또는 상태 변경
2. 또는 Application에 `@OnDelete(action = OnDeleteAction.CASCADE)` 추가

### 2.3 ⚠️ User 삭제 시 연관 엔티티 처리
**문제**: User 삭제 시 연관된 엔티티 처리 로직 없음
- ProjectPost.creator (nullable = false)
- Application.applicant (nullable = false)
- TeamMembers.userId (연관관계 없음)
- Project.ownerId (연관관계 없음)

**해결 방안**:
1. User 삭제 시 연관 엔티티 처리 로직 추가 (Soft delete 또는 상태 변경)
2. 또는 User 삭제를 제한하고 비활성화만 허용

### 2.4 ⚠️ Team 삭제 시 ProjectPost 처리
**문제**: Team 삭제 시 ProjectPost의 team_id가 null이 될 수 있음
- 현재: ProjectPost.team_id는 nullable = true이므로 문제 없음
- 하지만 명시적인 처리 필요

**해결 방안**:
1. Team 삭제 시 ProjectPost.team_id를 null로 설정
2. 또는 ProjectPost도 함께 삭제 (비즈니스 로직에 따라)

### 2.5 ⚠️ TeamMembers와 User 연관관계 부재
**문제**: TeamMembers가 User 엔티티와 직접 연관관계가 없음
- 현재: userId만 Long으로 저장
- User 정보 조회 시 별도 쿼리 필요

**해결 방안**:
1. TeamMembers에 `@ManyToOne` User 추가 (선택사항)
2. 또는 현재 방식 유지 (성능 고려)

### 2.6 ⚠️ Project와 User 연관관계 부재
**문제**: Project가 User 엔티티와 직접 연관관계가 없음
- 현재: ownerId만 Long으로 저장
- User 정보 조회 시 별도 쿼리 필요

**해결 방안**:
1. Project에 `@ManyToOne` User 추가 (선택사항)
2. 또는 현재 방식 유지 (성능 고려)

## 3. 현재 정상 동작하는 부분

### 3.1 ✅ Team과 TeamMembers 관계
- CascadeType.ALL, orphanRemoval = true 설정으로 자동 처리
- Team 삭제 시 TeamMembers도 함께 삭제됨

### 3.2 ✅ Team과 Meeting 관계
- CascadeType.ALL, orphanRemoval = true 설정으로 자동 처리
- Team 삭제 시 Meeting도 함께 삭제됨

### 3.3 ✅ ProjectPost와 User (creator) 관계
- `@ManyToOne` 관계 정상
- nullable = false로 필수 관계 보장

### 3.4 ✅ Application과 ProjectPost, User 관계
- `@ManyToOne` 관계 정상
- nullable = false로 필수 관계 보장

## 4. 권장 개선 사항

### 4.1 즉시 수정 필요 (High Priority)
1. **TeamMembers 삭제 시 TeamMemberLeaveRequest 처리**
   - TeamMembersService.delete()에서 관련 TeamMemberLeaveRequest 삭제 추가

2. **ProjectPost 삭제 시 Application 처리**
   - ProjectPostService.delete()에서 관련 Application 삭제 또는 상태 변경 추가

### 4.2 개선 권장 (Medium Priority)
1. **User 삭제 시 연관 엔티티 처리**
   - UserService에 삭제 전 검증 로직 추가
   - 또는 User 삭제를 제한하고 비활성화만 허용

2. **Team 삭제 시 ProjectPost 처리**
   - TeamService.delete()에서 ProjectPost.team_id를 null로 설정

### 4.3 선택 사항 (Low Priority)
1. **TeamMembers와 User 연관관계 추가**
   - 성능과 복잡성의 트레이드오프 고려

2. **Project와 User 연관관계 추가**
   - 성능과 복잡성의 트레이드오프 고려

## 5. 데이터 무결성 체크리스트

- [x] ProjectPost.creator는 항상 존재 (nullable = false)
- [x] Application.project는 항상 존재 (nullable = false)
- [x] Application.applicant는 항상 존재 (nullable = false)
- [x] Team.project는 항상 존재 (nullable = false, unique = true)
- [x] TeamMembers.team은 항상 존재 (nullable = false)
- [x] TeamMembers 삭제 시 TeamMemberLeaveRequest 처리 (TeamMembersService.delete()에서 처리)
- [x] ProjectPost 삭제 시 Application 처리 (ProjectPostService.delete()에서 처리)
- [x] User 삭제 시 연관 엔티티 처리 (UserService.deleteUser()에서 처리)
- [x] Team 삭제 시 ProjectPost 처리 (TeamService.delete()에서 처리)
- [x] team_invitations 테이블 정리 (DatabaseCleanupConfig에서 자동 처리)

