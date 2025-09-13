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
        save(User.builder().id(1L).name("Alice")
                .skills(List.of("Java","Spring","MySQL"))
                .interests(List.of("Backend","Finance")).build());
        save(User.builder().id(2L).name("Bob")
                .skills(List.of("Python","Django","PostgreSQL"))
                .interests(List.of("Backend","AI")).build());
        save(User.builder().id(3L).name("Carol")
                .skills(List.of("React","TypeScript","CSS"))
                .interests(List.of("Frontend","UX")).build());
        save(User.builder().id(4L).name("Dave")
                .skills(List.of("Java","Spring","Kafka"))
                .interests(List.of("Backend","Data")).build());
        save(User.builder().id(5L).name("Eve")
                .skills(List.of("Python","Pandas","ML"))
                .interests(List.of("AI","Data")).build());
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
