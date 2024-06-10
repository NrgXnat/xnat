/*
 * web: org.nrg.xnat.security.tokens.XnatDatabaseUsernamePasswordAuthenticationToken
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security.tokens;

import org.nrg.xdat.services.XdatUserAuthService;

public class XnatDatabaseUsernamePasswordAuthenticationToken extends AbstractXnatAuthenticationToken {
    public XnatDatabaseUsernamePasswordAuthenticationToken(final Object principal, final Object credentials) {
        super(XdatUserAuthService.LOCALDB, principal, credentials);
    }

    public String toString() {
        if (getPrincipal() != null) {
            return getPrincipal().toString();
        } else {
            return "";
        }
    }
}
