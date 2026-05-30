package io.github.softdelete.repository;

import io.github.softdelete.audit.SoftDeleteAuditorProvider;
import io.github.softdelete.entity.SoftDeletableEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link SoftDeleteRepository}.
 *
 * <p>Overrides all standard read operations to exclude soft-deleted rows,
 * and replaces {@code delete} with a soft delete. Hard deletes remain available
 * via the dedicated {@code hardDelete*} methods.
 *
 * <p>JPQL queries reference the {@code deleted} field defined in
 * {@link SoftDeletableEntity}. No SQL dialect knowledge is required.
 */
@Transactional(readOnly = true)
public class SoftDeleteRepositoryImpl<T extends SoftDeletableEntity, ID>
        extends SimpleJpaRepository<T, ID>
        implements SoftDeleteRepository<T, ID> {

    private final JpaEntityInformation<T, ?> entityInfo;
    private final EntityManager em;
    private final SoftDeleteAuditorProvider auditorProvider;

    public SoftDeleteRepositoryImpl(
            JpaEntityInformation<T, ?> entityInfo,
            EntityManager em,
            SoftDeleteAuditorProvider auditorProvider) {
        super(entityInfo, em);
        this.entityInfo = entityInfo;
        this.em = em;
        this.auditorProvider = auditorProvider;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** JPQL entity name (e.g. "Order", not "orders"). */
    private String entityName() {
        return entityInfo.getEntityName();
    }

    /** Java type for typed queries. */
    private Class<T> javaType() {
        return entityInfo.getJavaType();
    }

    /** Name of the single @Id attribute (composite keys not supported). */
    private String idAttributeName() {
        return entityInfo.getIdAttributeNames().iterator().next();
    }

    // -------------------------------------------------------------------------
    // Overridden standard reads — all exclude deleted rows
    // -------------------------------------------------------------------------

    @Override
    public Optional<T> findById(ID id) {
        String jpql = "SELECT e FROM " + entityName()
                + " e WHERE e." + idAttributeName() + " = :id AND e.deleted = false";
        TypedQuery<T> q = em.createQuery(jpql, javaType());
        q.setParameter("id", id);
        try {
            return Optional.of(q.getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    @Override
    public List<T> findAll() {
        String jpql = "SELECT e FROM " + entityName() + " e WHERE e.deleted = false";
        return em.createQuery(jpql, javaType()).getResultList();
    }

    @Override
    public long count() {
        String jpql = "SELECT COUNT(e) FROM " + entityName() + " e WHERE e.deleted = false";
        return em.createQuery(jpql, Long.class).getSingleResult();
    }

    @Override
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }

    // -------------------------------------------------------------------------
    // Soft-delete operations
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void softDeleteById(ID id) {
        findById(id).ifPresent(this::softDelete);
    }

    @Override
    @Transactional
    public void softDelete(T entity) {
        entity.markDeleted(auditorProvider.currentAuditor());
        em.merge(entity);
    }

    @Override
    @Transactional
    public void softDeleteAll(Iterable<? extends T> entities) {
        entities.forEach(e -> softDelete((T) e));
    }

    /**
     * Overrides JpaRepository#delete to perform a soft delete by default.
     * Use {@link #hardDelete(SoftDeletableEntity)} for a real DELETE.
     */
    @Override
    @Transactional
    public void delete(T entity) {
        softDelete(entity);
    }

    /**
     * Overrides JpaRepository#deleteById to perform a soft delete by default.
     * Use {@link #hardDeleteById(Object)} for a real DELETE.
     */
    @Override
    @Transactional
    public void deleteById(ID id) {
        softDeleteById(id);
    }

    @Override
    @Transactional
    public void deleteAll(Iterable<? extends T> entities) {
        softDeleteAll(entities);
    }

    @Override
    @Transactional
    public void deleteAll() {
        findAll().forEach(this::softDelete);
    }

    // -------------------------------------------------------------------------
    // Restore operations
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public Optional<T> restore(ID id) {
        Optional<T> found = findByIdIncludingDeleted(id);
        found.ifPresent(e -> {
            e.restore();
            em.merge(e);
        });
        return found;
    }

    @Override
    @Transactional
    public void restore(T entity) {
        entity.restore();
        em.merge(entity);
    }

    // -------------------------------------------------------------------------
    // Queries including soft-deleted rows
    // -------------------------------------------------------------------------

    @Override
    public Optional<T> findByIdIncludingDeleted(ID id) {
        String jpql = "SELECT e FROM " + entityName()
                + " e WHERE e." + idAttributeName() + " = :id";
        TypedQuery<T> q = em.createQuery(jpql, javaType());
        q.setParameter("id", id);
        try {
            return Optional.of(q.getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    @Override
    public List<T> findAllIncludingDeleted() {
        String jpql = "SELECT e FROM " + entityName() + " e";
        return em.createQuery(jpql, javaType()).getResultList();
    }

    @Override
    public List<T> findAllDeleted() {
        String jpql = "SELECT e FROM " + entityName() + " e WHERE e.deleted = true";
        return em.createQuery(jpql, javaType()).getResultList();
    }

    @Override
    public long countDeleted() {
        String jpql = "SELECT COUNT(e) FROM " + entityName() + " e WHERE e.deleted = true";
        return em.createQuery(jpql, Long.class).getSingleResult();
    }

    // -------------------------------------------------------------------------
    // Hard (permanent) delete
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void hardDeleteById(ID id) {
        findByIdIncludingDeleted(id).ifPresent(this::hardDelete);
    }

    @Override
    @Transactional
    public void hardDelete(T entity) {
        T managed = em.contains(entity) ? entity : em.merge(entity);
        em.remove(managed);
    }
}
