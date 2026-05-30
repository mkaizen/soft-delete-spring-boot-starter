package io.github.softdelete.annotation;

import java.lang.annotation.*;

/**
 * Marks a JPA entity as soft-deletable.
 *
 * <p>Entities annotated with {@code @SoftDeletable} must also extend
 * {@link io.github.softdelete.entity.SoftDeletableEntity}, which provides
 * the {@code deleted}, {@code deletedAt}, and {@code deletedBy} columns.
 *
 * <p>This annotation is a marker used by the framework; the actual behaviour
 * is provided by {@link io.github.softdelete.repository.SoftDeleteRepository}
 * and its auto-configured implementation.
 *
 * <pre>{@code
 * @Entity
 * @SoftDeletable
 * public class Product extends SoftDeletableEntity {
 *     @Id @GeneratedValue
 *     private Long id;
 *     private String name;
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SoftDeletable {
}
