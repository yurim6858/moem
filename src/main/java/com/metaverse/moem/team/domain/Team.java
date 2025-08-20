package com.metaverse.moem.team.domain;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 (외부 무분별 사용 금지용 PROTECTED)
@AllArgsConstructor
@Builder // 빌더 패턴 제공
@Entity
@Table(name = "teams") // DB Table 및 매핑, 테이블 이름 "teams"
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // PK, Auto_Increment
    private Long id;

    @Column(nullable = false, length = 60, unique = true)
    // 팀이름, 필수항목, 길이제한, 중복불가
    private String name;

    @Column(length = 255)
    // 간략한 팀설명
    private String description;

    @Column(nullable = false, updatable = false)
    // 팀 생성시점 기록
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist // INSERT 되기 전 자동 실행 → 생성일/수정일 초기화
    void onCreate(){ createdAt = LocalDateTime.now(); updatedAt = createdAt; }

    @PreUpdate  // UPDATE 되기 전 자동 실행 → 수정일 갱신
    void onUpdate(){ updatedAt = LocalDateTime.now(); }

}
