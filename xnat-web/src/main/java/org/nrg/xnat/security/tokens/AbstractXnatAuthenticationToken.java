package org.nrg.xnat.security.tokens;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class AbstractXnatAuthenticationToken extends UsernamePasswordAuthenticationToken implements XnatAuthenticationToken {
    protected AbstractXnatAuthenticationToken(final String providerId, final Object principal, final Object credentials) {
        super(principal, credentials);
        _providerId = providerId;
    }

    protected AbstractXnatAuthenticationToken(final String providerId, final Object principal, final Object credentials, final Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
        _providerId = providerId;
    }

    @Override
    public String getProviderId() {
        return _providerId;
    }

    private final String _providerId;
}
