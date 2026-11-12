/*
 * core: org.nrg.xdat.turbine.modules.actions.XDATRegisterUserTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.actions;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the registration parameter allowlist of {@link XDATRegisterUser}. Only the registration form's fields
 * (login, email, password, first and last name) are kept, matched by exact key. Other spellings XFT treats as the same
 * field (a different separator, an {@code ELEMENT_} wrapper, no separator) are dropped.
 */
public class XDATRegisterUserTest {

    private static Map<String, String> params(final String... keyValues) {
        final Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    @Test
    public void allowlistKeepsTheBasicProfileFields() {
        final Map<String, String> result = XDATRegisterUser.allowlistRegistrationParameters(params(
                "xdat:user.login", "someone",
                "xdat:user.email", "someone@example.org",
                "xdat:user.primary_password", "secret",
                "xdat:user.firstname", "Some",
                "xdat:user.lastname", "One"));
        assertThat(result).containsOnlyKeys(
                "xdat:user.login", "xdat:user.email", "xdat:user.primary_password",
                "xdat:user.firstname", "xdat:user.lastname");
    }

    @Test
    public void allowlistDropsEveryOtherKey() {
        // Other xdat:user fields in each spelling XFT accepts ('.', '/', an ELEMENT_ wrapper, no separator), plus
        // non-user parameters. containsOnlyKeys names any key that survives.
        final Map<String, String> result = XDATRegisterUser.allowlistRegistrationParameters(params(
                "xdat:user.login", "someone",
                "xdat:user.assigned_roles.assigned_role[0].role_name", "value",
                "xdat:user.groups.groupID[0]", "value",
                "xdat:user.element_access[0].element_name", "value",
                "xdat:user.enabled", "true",
                "xdat:user.verified", "true",
                "xdat:user/assigned_roles/assigned_role[0]/role_name", "value",
                "xdat:user/groups/groupID[0]", "value",
                "ELEMENT_xdat:user/groups/groupID[0]", "value",
                "xdat:usergroups", "value",
                "operation", "register",
                "XNAT_CSRF", "token-value",
                "authmethod", "localdb"));
        assertThat(result).containsOnlyKeys("xdat:user.login");
    }

    @Test
    public void allowlistKeepsOnlyTheExactKeyAndDropsEquivalentAliases() {
        // An alias XFT treats as the same field (here '@' instead of '.') is dropped, not folded onto the canonical
        // key, so the login that builds the user is the one the auth record and duplicate check read.
        final Map<String, String> result = XDATRegisterUser.allowlistRegistrationParameters(params(
                "xdat:user.login", "first",
                "xdat:user@login", "second"));
        assertThat(result).containsOnlyKeys("xdat:user.login");
        assertThat(result).containsEntry("xdat:user.login", "first");
    }

    @Test
    public void allowlistDropsSlashFormOfABasicField() {
        // Another spelling of an allowed field is dropped too; the registration form submits the dot key.
        final Map<String, String> result = XDATRegisterUser.allowlistRegistrationParameters(params(
                "xdat:user/email", "someone@example.org"));
        assertThat(result).isEmpty();
    }
}
