package com.metaverse.moem.team.domain;


import com.metaverse.moem.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 (외부 무분별 사용 금지용 PROTECTED)
@AllArgsConstructor
@Builder // 빌더 패턴 제공
@Entity
@Table(name = "teams") // DB Table 및 매핑, 테이블 이름 "teams"
public class Team extends BaseTimeEntity {
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

}
