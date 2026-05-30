package io.github.softdelete.audit;

/**
 * Default {@link SoftDeleteAuditorProvider} that always returns {@code "system"}.
 *
 * <p>This bean is registered by auto-configuration with
 * {@code @ConditionalOnMissingBean}, so declaring your own
 * {@code SoftDeleteAuditorProvider} bean is all that is needed to override it.
 */
public class SystemAuditorProvider implements SoftDeleteAuditorProvider {

    @Override
    public String currentAuditor() {
        return "system";
    }
}
