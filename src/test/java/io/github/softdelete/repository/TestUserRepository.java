package io.github.softdelete.repository;

import io.github.softdelete.entity.TestUser;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestUserRepository extends SoftDeleteRepository<TestUser, Long> {

    // Derived query — Spring Data generates this; it runs against the *live*
    // view (soft-deleted rows are excluded by the overridden findAll / count).
    Optional<TestUser> findByUsername(String username);
}
