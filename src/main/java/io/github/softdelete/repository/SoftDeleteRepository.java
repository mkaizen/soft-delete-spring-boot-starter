package io.github.softdelete.repository;

import io.github.softdelete.entity.SoftDeletableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * Drop-in replacement for {@link JpaRepository} for soft-deletable entities.
 *
 * <p>All standard read operations ({@code findById}, {@code findAll}, etc.)
 * automatically exclude soft-deleted records. Use the {@code *IncludingDeleted}
 * variants when you need to see deleted rows.
 *
 * <p>The standard {@code delete} / {@code deleteById} methods are overridden
 * to perform a soft delete rather than a hard {@code DELETE} statement.
 * Use {@link #hardDeleteById(Object)} or {@link #hardDelete(SoftDeletableEntity)}
 * when you genuinely need to remove a row from the database.
 *
 * <pre>{@code
 * public interface OrderRepository
 *         extends SoftDeleteRepository<Order, Long> {
 *     // Spring Data derived queries work as normal:
 *     List<Order> findByCustomerId(Long customerId);
 * }
 * }</pre>
 *
 * @param <T>  entity type, must extend {@link SoftDeletableEntity}
 * @param <ID> primary-key type
 */
@NoRepositoryBean
public interface SoftDeleteRepository<T extends SoftDeletableEntity, ID>
        extends JpaRepository<T, ID> {

    // -------------------------------------------------------------------------
    // Soft-delete operations
    // -------------------------------------------------------------------------

    /**
     * Soft-deletes the entity with the given id.
     * Sets {@code deleted = true}, {@code deletedAt}, and {@code deletedBy}.
     * No-op if the id does not exist or the record is already deleted.
     */
    void softDeleteById(ID id);

    /**
     * Soft-deletes the given entity instance.
     */
    void softDelete(T entity);

    /**
     * Soft-deletes all entities in the given iterable.
     */
    void softDeleteAll(Iterable<? extends T> entities);

    // -------------------------------------------------------------------------
    // Restore operations
    // -------------------------------------------------------------------------

    /**
     * Restores a previously soft-deleted entity, clearing all delete audit fields.
     * No-op if the entity is not currently soft-deleted.
     *
     * @return the restored entity, or {@link Optional#empty()} if not found
     */
    Optional<T> restore(ID id);

    /**
     * Restores the given entity instance.
     */
    void restore(T entity);

    // -------------------------------------------------------------------------
    // Queries that include soft-deleted records
    // -------------------------------------------------------------------------

    /**
     * Finds an entity by id regardless of its deleted state.
     */
    Optional<T> findByIdIncludingDeleted(ID id);

    /**
     * Returns all entities, including those that have been soft-deleted.
     */
    List<T> findAllIncludingDeleted();

    /**
     * Returns all soft-deleted entities only.
     */
    List<T> findAllDeleted();

    /**
     * Counts only the soft-deleted rows.
     */
    long countDeleted();

    // -------------------------------------------------------------------------
    // Hard (permanent) delete — escape hatch
    // -------------------------------------------------------------------------

    /**
     * Permanently removes the entity with the given id from the database.
     * This issues a real {@code DELETE} statement and cannot be undone.
     */
    void hardDeleteById(ID id);

    /**
     * Permanently removes the given entity from the database.
     */
    void hardDelete(T entity);
}
