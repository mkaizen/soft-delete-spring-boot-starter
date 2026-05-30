package io.github.softdelete.annotation;

import io.github.softdelete.config.SoftDeleteAutoConfiguration;
import io.github.softdelete.repository.SoftDeleteRepositoryFactoryBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.lang.annotation.*;

/**
 * Convenience annotation to activate soft-delete support.
 *
 * <p>Add to your Spring Boot application class (or any {@code @Configuration}
 * class) to enable the custom repository factory and auto-configuration:
 *
 * <pre>{@code
 * @SpringBootApplication
 * @EnableSoftDelete
 * public class MyApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyApplication.class, args);
 *     }
 * }
 * }</pre>
 *
 * <p>If you already use {@code @EnableJpaRepositories}, add
 * {@code repositoryFactoryBeanClass = SoftDeleteRepositoryFactoryBean.class}
 * to it instead of using this annotation.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SoftDeleteAutoConfiguration.class)
@EnableJpaRepositories(repositoryFactoryBeanClass = SoftDeleteRepositoryFactoryBean.class)
public @interface EnableSoftDelete {
}
