package com.metaverse.moem.matching.repository;

import com.metaverse.moem.matching.domain.User;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile({"default","mock"})
public class MockUserRepository implements UserRepository{

    private final Map<Long, User> store = new ConcurrentHashMap<>();

    @PostConstruct
    void seed() {
        save(User.builder().id(1L).name("Kang")
                .role("Frontend Developer").university("SNU").location("Seoul")
                .note("Frontend Developer 관련 경험 보유, 협업 능력 우수")
                .skills(List.of("React", "TypeScript", "UX/UI"))
                .interests(List.of("Frontend", "Design"))
                .matchScore(86).build());

        save(User.builder().id(2L).name("Kim")
                .role("Backend Developer").university("POSTECH").location("Pohang")
                .note("Backend Developer 관련 경험 보유, 협업 능력 우수")
                .skills(List.of("Spring", "MySQL", "JPA"))
                .interests(List.of("Backend", "Database"))
                .matchScore(83).build());

        save(User.builder().id(3L).name("Lee")
                .role("Security Engineer").university("SNU").location("Busan")
                .note("Security Engineer 관련 경험 보유, 협업 능력 우수")
                .skills(List.of("PenTest", "Cryptography"))
                .interests(List.of("Security", "Network"))
                .matchScore(93).build());

        save(User.builder().id(4L).name("Yoon")
                .role("Project Manager").university("Hanyang").location("Daejeon")
                .note("Project Manager 관련 경험 보유, 협업 능력 우수")
                .skills(List.of("Jira", "Notion"))
                .interests(List.of("Agile", "Planning"))
                .matchScore(90).build());

        save(User.builder().id(5L).name("Park")
                .role("AI Engineer").university("KAIST").location("Daejeon")
                .note("AI Engineer 관련 경험 보유, 협업 능력 우수")
                .skills(List.of("PyTorch", "Transformers"))
                .interests(List.of("NLP", "Vision"))
                .matchScore(95).build());
    }

    @Override
    public Optional<User> findById(Long Id) {
        return Optional.ofNullable(store.get(Id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId((long) (store.size() + 1));
        }
        store.put(user.getId(), user);
        return user;
    }


}
