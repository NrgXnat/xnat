/*
 * web: org.nrg.xnat.security.alias.AliasTokenAuthenticationToken
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security.alias;

import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xnat.security.tokens.AbstractXnatAuthenticationToken;

public class AliasTokenAuthenticationToken extends AbstractXnatAuthenticationToken {
    public AliasTokenAuthenticationToken(final Object principal, final Object credentials) {
        super(XdatUserAuthService.TOKEN, principal, credentials);
    }

    public String getAlias() {
        return (String) getPrincipal();
    }

    public String getSecret() {
        return (String) getCredentials();
    }

    @Override
    public String toString() {
        return getPrincipal().toString();
    }
}
