package io.github.softdelete.config;

import io.github.softdelete.audit.SoftDeleteAuditorProvider;
import io.github.softdelete.audit.SystemAuditorProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for the soft-delete starter.
 *
 * <p>Registers a default {@link SoftDeleteAuditorProvider} ({@code "system"})
 * that is replaced whenever the application provides its own bean of that type.
 *
 * <p>The repository factory bean is activated separately via
 * {@link io.github.softdelete.annotation.EnableSoftDelete} or by adding
 * {@code repositoryFactoryBeanClass = SoftDeleteRepositoryFactoryBean.class}
 * to your {@code @EnableJpaRepositories} annotation.
 */
@AutoConfiguration(after = HibernateJpaAutoConfiguration.class)
public class SoftDeleteAutoConfiguration {

    /**
     * Default auditor provider. Override by declaring a
     * {@code SoftDeleteAuditorProvider} bean in your application context.
     */
    @Bean
    @ConditionalOnMissingBean(SoftDeleteAuditorProvider.class)
    public SoftDeleteAuditorProvider softDeleteAuditorProvider() {
        return new SystemAuditorProvider();
    }
}
