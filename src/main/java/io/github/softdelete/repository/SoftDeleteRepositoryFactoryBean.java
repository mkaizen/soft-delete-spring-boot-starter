package io.github.softdelete.repository;

import io.github.softdelete.audit.SoftDeleteAuditorProvider;
import io.github.softdelete.entity.SoftDeletableEntity;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

/**
 * Custom {@link JpaRepositoryFactoryBean} that substitutes
 * {@link SoftDeleteRepositoryImpl} for any repository that extends
 * {@link SoftDeleteRepository}.
 *
 * <p>Register via {@code @EnableJpaRepositories}:
 * <pre>{@code
 * @EnableJpaRepositories(
 *     repositoryFactoryBeanClass = SoftDeleteRepositoryFactoryBean.class
 * )
 * }</pre>
 * or simply annotate your application class with {@code @EnableSoftDelete}.
 */
public class SoftDeleteRepositoryFactoryBean<R extends SoftDeleteRepository<T, ID>,
        T extends SoftDeletableEntity, ID extends Serializable>
        extends JpaRepositoryFactoryBean<R, T, ID> {

    @Autowired
    private SoftDeleteAuditorProvider auditorProvider;

    public SoftDeleteRepositoryFactoryBean(Class<? extends R> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager em) {
        return new SoftDeleteRepositoryFactory<>(em, auditorProvider);
    }

    // -------------------------------------------------------------------------
    // Inner factory
    // -------------------------------------------------------------------------

    private static class SoftDeleteRepositoryFactory<T extends SoftDeletableEntity, ID>
            extends JpaRepositoryFactory {

        private final EntityManager em;
        private final SoftDeleteAuditorProvider auditorProvider;

        SoftDeleteRepositoryFactory(EntityManager em, SoftDeleteAuditorProvider auditorProvider) {
            super(em);
            this.em = em;
            this.auditorProvider = auditorProvider;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected JpaRepositoryImplementation<?, ?> getTargetRepository(
                RepositoryInformation info, EntityManager em) {

            JpaEntityInformation<T, ID> entityInfo =
                    (JpaEntityInformation<T, ID>) getEntityInformation(info.getDomainType());

            return new SoftDeleteRepositoryImpl<>(entityInfo, em, auditorProvider);
        }

        @Override
        protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
            return SoftDeleteRepositoryImpl.class;
        }
    }
}
