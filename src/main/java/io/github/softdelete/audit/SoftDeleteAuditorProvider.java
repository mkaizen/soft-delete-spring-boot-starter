package io.github.softdelete.audit;

/**
 * Strategy interface for resolving the current principal name to be stored
 * in the {@code deletedBy} column at soft-delete time.
 *
 * <p>A default implementation ({@link SystemAuditorProvider}) is registered
 * by auto-configuration and always returns {@code "system"}. Override it by
 * exposing your own {@code @Bean} of this type:
 *
 * <pre>{@code
 * @Bean
 * public SoftDeleteAuditorProvider softDeleteAuditorProvider() {
 *     return () -> SecurityContextHolder.getContext()
 *                      .getAuthentication()
 *                      .getName();
 * }
 * }</pre>
 */
@FunctionalInterface
public interface SoftDeleteAuditorProvider {

    /**
     * Returns the name of the current principal, or a fallback string if no
     * authenticated user is available. Must never return {@code null}.
     */
    String currentAuditor();
}
