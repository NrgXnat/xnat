package org.nrg.xnat.security.provider;

import java.util.Properties;

/**
 * Defines two ways to validate the settings for an authentication provider implementation.
 *
 * @param <P> An implementation of the {@link XnatAuthenticationProvider} interface.
 */
public interface XnatAuthenticationProviderValidator<P extends XnatAuthenticationProvider> {
    /**
     * Validates the settings for the specified authentication provider.
     *
     * @param provider The provider instance to be validated.
     *
     * @return Results of the authentication validation.
     */
    String validate(final P provider);

    /**
     * Validates the specified settings using the implementing authentication provider mechanism.
     *
     * @param properties The properties to configure for validation.
     *
     * @return Results of the authentication validation.
     */
    String validate(final Properties properties);
}
