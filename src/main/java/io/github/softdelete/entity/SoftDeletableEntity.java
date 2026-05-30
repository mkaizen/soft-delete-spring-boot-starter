package io.github.softdelete.entity;

import io.github.softdelete.annotation.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.LocalDateTime;

/**
 * Base class for all soft-deletable JPA entities.
 *
 * <p>Extend this class and annotate with {@link SoftDeletable} to gain:
 * <ul>
 *   <li>{@code deleted} — boolean flag, always {@code false} for live records</li>
 *   <li>{@code deletedAt} — timestamp set when the record is soft-deleted</li>
 *   <li>{@code deletedBy} — principal name set at deletion time</li>
 * </ul>
 *
 * <p>The setters for audit fields are package-private; use the repository
 * methods ({@code softDeleteById}, {@code restore}) to mutate state rather
 * than calling them directly.
 *
 * <pre>{@code
 * @Entity
 * @SoftDeletable
 * public class Order extends SoftDeletableEntity {
 *     @Id @GeneratedValue
 *     private Long id;
 *
 *     private String reference;
 * }
 * }</pre>
 */
@MappedSuperclass
public abstract class SoftDeletableEntity {

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 255)
    private String deletedBy;

    // -------------------------------------------------------------------------
    // Public read accessors
    // -------------------------------------------------------------------------

    public boolean isDeleted() {
        return deleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    // -------------------------------------------------------------------------
    // Package-private write accessors (used by SoftDeleteRepositoryImpl)
    // -------------------------------------------------------------------------

    void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    // -------------------------------------------------------------------------
    // Convenience state-transition helpers (called by the repository impl)
    // -------------------------------------------------------------------------

    /**
     * Marks this entity as soft-deleted with the provided auditor name.
     * Prefer calling {@code repository.softDeleteById(id)} over this directly.
     */
    public void markDeleted(String auditor) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = auditor;
    }

    /**
     * Clears soft-delete state, effectively restoring the entity.
     * Prefer calling {@code repository.restore(id)} over this directly.
     */
    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }
}
