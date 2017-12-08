package org.nrg.xnat.security.conditions;

import org.nrg.xdat.services.XdatUserAuthService;

public class LdapAuthProvidersDefinedCondition extends AuthProvidersDefinedCondition {
    protected LdapAuthProvidersDefinedCondition() {
        super(XdatUserAuthService.LDAP);
    }
}
