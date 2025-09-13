package com.metaverse.moem.matching.domain;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;
    private String  name;
    private List<String> skills;
    private List<String> interests;
}
