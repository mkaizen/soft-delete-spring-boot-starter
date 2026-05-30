package io.github.softdelete.repository;

import io.github.softdelete.TestApplication;
import io.github.softdelete.entity.TestUser;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@Transactional
@DisplayName("SoftDeleteRepository")
class SoftDeleteRepositoryTest {

    @Autowired
    TestUserRepository repo;

    private TestUser alice;
    private TestUser bob;
    private TestUser carol;

    @BeforeEach
    void setUp() {
        alice = repo.save(new TestUser("alice", "alice@example.com"));
        bob   = repo.save(new TestUser("bob",   "bob@example.com"));
        carol = repo.save(new TestUser("carol", "carol@example.com"));
    }

    // -------------------------------------------------------------------------
    // findAll / count — live records only
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findAll() excludes soft-deleted entities")
    void findAll_excludesDeleted() {
        repo.softDeleteById(alice.getId());

        List<TestUser> live = repo.findAll();

        assertThat(live).hasSize(2)
                .extracting(TestUser::getUsername)
                .containsExactlyInAnyOrder("bob", "carol");
    }

    @Test
    @DisplayName("count() reflects only live rows")
    void count_excludesDeleted() {
        repo.softDelete(alice);
        repo.softDelete(bob);

        assertThat(repo.count()).isEqualTo(1L);
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findById() returns empty for a soft-deleted entity")
    void findById_returnsEmptyAfterSoftDelete() {
        repo.softDeleteById(alice.getId());

        assertThat(repo.findById(alice.getId())).isEmpty();
    }

    @Test
    @DisplayName("findById() still finds a live entity")
    void findById_findsLiveEntity() {
        assertThat(repo.findById(bob.getId()))
                .isPresent()
                .get()
                .extracting(TestUser::getUsername)
                .isEqualTo("bob");
    }

    // -------------------------------------------------------------------------
    // Soft-delete audit fields
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("softDeleteById() populates audit fields")
    void softDelete_populatesAuditFields() {
        repo.softDeleteById(alice.getId());

        TestUser deleted = repo.findByIdIncludingDeleted(alice.getId()).orElseThrow();

        assertThat(deleted.isDeleted()).isTrue();
        assertThat(deleted.getDeletedAt()).isNotNull();
        assertThat(deleted.getDeletedBy()).isEqualTo("system"); // default auditor
    }

    @Test
    @DisplayName("softDelete(entity) is idempotent")
    void softDelete_idempotent() {
        repo.softDeleteById(alice.getId());
        repo.softDeleteById(alice.getId()); // second call should be a no-op

        assertThat(repo.countDeleted()).isEqualTo(1L);
    }

    // -------------------------------------------------------------------------
    // Restore
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("restore() clears all audit fields and makes record visible again")
    void restore_clearsAuditFields() {
        repo.softDeleteById(alice.getId());
        repo.restore(alice.getId());

        Optional<TestUser> restored = repo.findById(alice.getId());
        assertThat(restored).isPresent();

        TestUser u = restored.get();
        assertThat(u.isDeleted()).isFalse();
        assertThat(u.getDeletedAt()).isNull();
        assertThat(u.getDeletedBy()).isNull();
    }

    @Test
    @DisplayName("restore() returns empty Optional for unknown id")
    void restore_unknownId_returnsEmpty() {
        assertThat(repo.restore(999L)).isEmpty();
    }

    // -------------------------------------------------------------------------
    // IncludingDeleted queries
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findAllIncludingDeleted() returns every row")
    void findAllIncludingDeleted_returnsAll() {
        repo.softDeleteById(alice.getId());

        assertThat(repo.findAllIncludingDeleted()).hasSize(3);
    }

    @Test
    @DisplayName("findAllDeleted() returns only soft-deleted rows")
    void findAllDeleted_returnsOnlyDeleted() {
        repo.softDeleteById(alice.getId());
        repo.softDeleteById(carol.getId());

        List<TestUser> deleted = repo.findAllDeleted();
        assertThat(deleted).hasSize(2)
                .extracting(TestUser::getUsername)
                .containsExactlyInAnyOrder("alice", "carol");
    }

    @Test
    @DisplayName("countDeleted() counts only soft-deleted rows")
    void countDeleted_correct() {
        assertThat(repo.countDeleted()).isZero();

        repo.softDeleteById(bob.getId());
        assertThat(repo.countDeleted()).isEqualTo(1L);
    }

    // -------------------------------------------------------------------------
    // Hard delete
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("hardDeleteById() permanently removes the row")
    void hardDelete_removesRow() {
        repo.hardDeleteById(alice.getId());

        assertThat(repo.findByIdIncludingDeleted(alice.getId())).isEmpty();
        assertThat(repo.findAllIncludingDeleted()).hasSize(2);
    }

    @Test
    @DisplayName("hardDelete() works on a previously soft-deleted entity")
    void hardDelete_afterSoftDelete() {
        repo.softDeleteById(alice.getId());
        repo.hardDeleteById(alice.getId());

        assertThat(repo.findByIdIncludingDeleted(alice.getId())).isEmpty();
    }

    // -------------------------------------------------------------------------
    // delete() / deleteById() redirect to soft delete
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("delete() performs a soft delete, not a hard delete")
    void delete_performsSoftDelete() {
        repo.delete(alice);

        assertThat(repo.findById(alice.getId())).isEmpty();
        assertThat(repo.findByIdIncludingDeleted(alice.getId())).isPresent();
    }

    @Test
    @DisplayName("deleteById() performs a soft delete, not a hard delete")
    void deleteById_performsSoftDelete() {
        repo.deleteById(alice.getId());

        assertThat(repo.findById(alice.getId())).isEmpty();
        assertThat(repo.findAllIncludingDeleted()).hasSize(3); // row still in DB
    }

    // -------------------------------------------------------------------------
    // existsById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("existsById() returns false for a soft-deleted entity")
    void existsById_falseAfterSoftDelete() {
        repo.softDeleteById(alice.getId());

        assertThat(repo.existsById(alice.getId())).isFalse();
    }
}
