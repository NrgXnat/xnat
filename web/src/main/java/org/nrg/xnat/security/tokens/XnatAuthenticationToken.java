package org.nrg.xnat.security.tokens;

import org.springframework.security.core.Authentication;

public interface XnatAuthenticationToken extends Authentication {
    String getProviderId();
}
