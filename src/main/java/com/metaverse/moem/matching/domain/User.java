package com.metaverse.moem.matching.domain;

import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String name;

    private String role;
    private String university;
    private String location;
    private String note;

    private List<String> skills;
    private List<String> interests;

    private Integer matchScore;
}