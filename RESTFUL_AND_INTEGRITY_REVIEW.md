# RESTful API 설계 및 데이터 무결성 검토 보고서

## 1. RESTful API 설계 검토

### 1.1 ✅ 잘 설계된 부분

#### 1.1.1 HTTP 메서드 사용
- **GET**: 조회 작업에 적절히 사용
- **POST**: 생성 작업에 적절히 사용 (201 Created 반환)
- **PUT**: 전체 업데이트에 사용
- **PATCH**: 부분 업데이트에 사용 (`/applications/{id}/status`)
- **DELETE**: 삭제 작업에 적절히 사용 (204 No Content 반환)

#### 1.1.2 리소스 중심 URL 설계
```
✅ /api/applications - 지원서 리소스
✅ /api/teams/{teamId} - 팀 리소스
✅ /api/teams/{teamId}/members - 팀원 리소스 (중첩 리소스)
✅ /api/project-posts/{id} - 공고 리소스
✅ /api/projects/{id} - 프로젝트 리소스
```

#### 1.1.3 적절한 HTTP 상태 코드
- `201 Created`: 리소스 생성 성공
- `200 OK`: 조회/업데이트 성공
- `204 No Content`: 삭제 성공
- `400 Bad Request`: 잘못된 요청
- `401 Unauthorized`: 인증 실패
- `404 Not Found`: 리소스 없음

### 1.2 ⚠️ 개선이 필요한 부분

#### 1.2.1 비RESTful 엔드포인트

**문제 1: 동사 사용**
```java
// ❌ 비RESTful
POST /api/applications/{applicationId}/approve
POST /api/teams/{teamId}/start-project
PUT /api/projects/{id}/end
PUT /api/teams/{leaveRequestId}/respond
```

**개선 방안**:
```java
// ✅ RESTful
PATCH /api/applications/{applicationId}  // body: {status: "APPROVED"}
PATCH /api/projects/{id}  // body: {status: "STARTED"}
PATCH /api/projects/{id}  // body: {status: "ENDED"}
PATCH /api/teams/{teamId}/leave-requests/{leaveRequestId}  // body: {status: "APPROVED"}
```

**문제 2: 중첩 리소스의 동사 사용**
```java
// ❌ 비RESTful
POST /api/teams/{teamId}/leave-requests/members/{memberId}
```

**개선 방안**:
```java
// ✅ RESTful
POST /api/teams/{teamId}/leave-requests  // body: {memberId: ..., reason: ...}
```

#### 1.2.2 일관성 없는 경로 구조

**문제**:
```java
// 혼재된 경로 패턴
GET /api/applications/project/{projectId}  // 리소스/필터
GET /api/applications/user  // 리소스/필터 (쿼리 파라미터 권장)
GET /api/teams/my  // 리소스/동사
GET /api/projects/my  // 리소스/동사
```

**개선 방안**:
```java
// ✅ 일관된 경로 패턴
GET /api/applications?projectId={projectId}  // 쿼리 파라미터 사용
GET /api/applications?userId={userId}  // 쿼리 파라미터 사용
GET /api/teams?my=true  // 쿼리 파라미터 사용
GET /api/projects?my=true  // 쿼리 파라미터 사용
```

#### 1.2.3 특수 리소스 경로

**문제**:
```java
// 특수 경로가 많음
GET /api/applications/project/{projectId}/by-position
GET /api/teams/{teamId}/start-ready/{projectId}
GET /api/teams/{teamId}/leave-requests/pending
```

**개선 방안**:
```java
// ✅ 쿼리 파라미터 또는 필터 사용
GET /api/applications?projectId={projectId}&groupBy=position
GET /api/teams/{teamId}/start-ready?projectId={projectId}
GET /api/teams/{teamId}/leave-requests?status=PENDING
```

#### 1.2.4 HTTP 메서드와 작업의 불일치

**문제**:
```java
// 상태 변경을 POST로 처리
POST /api/applications/{applicationId}/approve  // 상태 변경은 PATCH가 적절
POST /api/teams/{teamId}/start-project  // 상태 변경은 PATCH가 적절
```

**개선 방안**:
```java
// ✅ 상태 변경은 PATCH 사용
PATCH /api/applications/{applicationId}  // body: {status: "APPROVED"}
PATCH /api/projects/{id}  // body: {status: "STARTED"}
```

### 1.3 RESTful 원칙 준수도

| 원칙 | 준수도 | 비고 |
|------|--------|------|
| 리소스 중심 URL | ⭐⭐⭐⭐ | 대부분 잘 지켜짐 |
| HTTP 메서드 적절 사용 | ⭐⭐⭐ | 일부 동사 사용 |
| 상태 코드 적절 사용 | ⭐⭐⭐⭐⭐ | 매우 잘 지켜짐 |
| Stateless | ⭐⭐⭐⭐⭐ | 완벽 |
| HATEOAS | ⭐⭐ | 미구현 (선택사항) |

## 2. 데이터 무결성 검토

### 2.1 ✅ 잘 구현된 부분

#### 2.1.1 외래키 제약조건
```java
// 필수 관계 보장
@JoinColumn(name = "creator_id", nullable = false)  // ProjectPost.creator
@JoinColumn(name = "project_id", nullable = false)  // Application.project
@JoinColumn(name = "applicant_id", nullable = false)  // Application.applicant
@JoinColumn(name = "project_id", nullable = false, unique = true)  // Team.project
@JoinColumn(name = "team_id", nullable = false)  // TeamMembers.team
```

#### 2.1.2 Cascade 설정
```java
// 자동 삭제 처리
@OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
private List<TeamMembers> members;  // Team 삭제 시 TeamMembers 자동 삭제

@OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Meeting> meetings;  // Team 삭제 시 Meeting 자동 삭제
```

#### 2.1.3 트랜잭션 처리
```java
@Transactional  // Service 레이어에 적절히 적용
@Transactional(readOnly = true)  // 조회 작업에 적용
```

#### 2.1.4 유니크 제약조건
```java
@UniqueConstraint(name = "uk_team_member_team_user", columnNames = {"team_id", "user_id"})
// TeamMembers: 같은 팀에 같은 사용자 중복 방지
```

### 2.2 ⚠️ 개선이 필요한 부분

#### 2.2.1 삭제 시 연관 엔티티 처리 (일부 수정 완료)

**수정 완료**:
- ✅ TeamMembers 삭제 시 TeamMemberLeaveRequest 삭제
- ✅ ProjectPost 삭제 시 Application 상태 변경

**추가 개선 필요**:
- ⚠️ User 삭제 시 연관 엔티티 처리 없음
  - ProjectPost.creator (nullable = false)
  - Application.applicant (nullable = false)
  - TeamMembers.userId
  - Project.ownerId

**권장 방안**:
```java
// UserService에 삭제 전 검증 추가
public void delete(Long userId) {
    // 연관 엔티티 확인
    if (projectPostRepository.existsByCreator_Id(userId)) {
        throw new IllegalStateException("작성한 공고가 있어 삭제할 수 없습니다.");
    }
    if (applicationRepository.existsByApplicant_Id(userId)) {
        throw new IllegalStateException("지원한 이력이 있어 삭제할 수 없습니다.");
    }
    // ... 기타 검증
    userRepository.deleteById(userId);
}
```

#### 2.2.2 외래키 제약조건 누락 가능성

**문제**:
- TeamMembers.userId: User 엔티티와 연관관계 없음 (Long만 저장)
- Project.ownerId: User 엔티티와 연관관계 없음 (Long만 저장)

**영향**:
- User 삭제 시 참조 무결성 위반 가능
- 데이터베이스 레벨에서 제약조건 없음

**권장 방안**:
```java
// Option 1: 연관관계 추가
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;  // TeamMembers

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "owner_id", nullable = false)
private User owner;  // Project

// Option 2: 현재 방식 유지하되, 애플리케이션 레벨에서 검증 강화
```

#### 2.2.3 Soft Delete와 Hard Delete 혼재

**현재 상태**:
- ProjectPost: Hard Delete (실제 삭제)
- Project: Soft Delete (`isDeleted` 플래그)
- User: Hard Delete (추정)

**문제**:
- 일관성 없는 삭제 전략
- 참조 무결성 문제 가능

**권장 방안**:
```java
// 일관된 삭제 전략 수립
// Option 1: 모두 Soft Delete
// Option 2: 중요 엔티티는 Soft Delete, 관계 엔티티는 Hard Delete
// Option 3: 비즈니스 로직에 따라 결정 (현재 방식 유지)
```

#### 2.2.4 트랜잭션 범위 검토 필요

**현재**:
- Service 메서드에 `@Transactional` 적용
- 일부 복잡한 작업에서 트랜잭션 범위 확인 필요

**권장**:
```java
// 복잡한 작업은 명시적 트랜잭션 관리
@Transactional
public void complexOperation() {
    // 여러 엔티티 변경 작업
    // 롤백 가능성 고려
}
```

### 2.3 데이터 무결성 체크리스트

| 항목 | 상태 | 비고 |
|------|------|------|
| 외래키 제약조건 | ✅ | 대부분 잘 설정됨 |
| 유니크 제약조건 | ✅ | 적절히 설정됨 |
| Cascade 삭제 | ✅ | Team 관련 잘 설정됨 |
| 트랜잭션 처리 | ✅ | 적절히 적용됨 |
| 삭제 시 연관 엔티티 처리 | ⚠️ | 일부 개선 필요 |
| Soft/Hard Delete 일관성 | ⚠️ | 전략 수립 필요 |
| User 삭제 제약 | ❌ | 처리 로직 없음 |

## 3. 종합 평가 및 권장 사항

### 3.1 RESTful API 설계

**점수: 75/100**

**강점**:
- 기본적인 RESTful 원칙 준수
- 적절한 HTTP 메서드 사용
- 명확한 상태 코드 사용

**개선 필요**:
1. 동사 사용 제거 (approve, start-project 등)
2. 쿼리 파라미터 활용 (필터링)
3. 상태 변경은 PATCH 사용

### 3.2 데이터 무결성

**점수: 80/100**

**강점**:
- 외래키 제약조건 잘 설정
- Cascade 삭제 적절히 사용
- 트랜잭션 처리 적절

**개선 필요**:
1. User 삭제 시 연관 엔티티 처리
2. Soft/Hard Delete 전략 수립
3. TeamMembers/Project와 User 연관관계 고려

### 3.3 우선순위별 개선 사항

#### High Priority (즉시 수정)
1. ✅ TeamMembers 삭제 시 TeamMemberLeaveRequest 처리 (완료)
2. ✅ ProjectPost 삭제 시 Application 처리 (완료)
3. User 삭제 시 연관 엔티티 검증 추가

#### Medium Priority (단기 개선)
1. RESTful 엔드포인트 개선 (동사 제거)
2. 쿼리 파라미터 활용
3. Soft/Hard Delete 전략 수립

#### Low Priority (장기 개선)
1. HATEOAS 구현 (선택사항)
2. API 버전 관리
3. 상세한 에러 응답 구조화

