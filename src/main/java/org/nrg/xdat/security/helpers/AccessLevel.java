/*
 * core: org.nrg.xdat.security.helpers.AccessLevel
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.helpers;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xapi.authorization.*;
import org.nrg.xapi.rest.XapiRequestMapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Defines the access levels available for various XNAT resources. These can be used with the {@link
 * XapiRequestMapping#restrictTo()} attribute and other permissions operations.
 */
@SuppressWarnings("DeprecatedIsStillUsed")
public enum AccessLevel {
    Null(null),
    Authenticated("authenticated", AuthenticatedXapiAuthorization.class),
    User("user", UserXapiAuthorization.class),
    Role("role", RoleXapiAuthorization.class),
    Admin("admin", AdminXapiAuthorization.class),
    DataAdmin("dataAdmin", AdminXapiAuthorization.class),
    DataAccess("dataAccess", AdminXapiAuthorization.class),
    Read("read", DataObjectXapiAuthorization.class),
    Edit("edit", DataObjectXapiAuthorization.class),
    Delete("delete", DataObjectXapiAuthorization.class),
    /**
     * Indicates that the user must belong to the collaborator group for the specified project.
     *
     * @deprecated Specify the level of access for a project or other data object through {@link #Read},
     *         {@link #Edit}, or {@link #Delete} instead
     */
    @Deprecated
    Collaborator("collaborator", DataObjectXapiAuthorization.class),
    /**
     * Indicates that the user must belong to the member group for the specified project.
     *
     * @deprecated Specify the level of access for a project or other data object through {@link #Read},
     *         {@link #Edit}, or {@link #Delete} instead
     */
    @Deprecated
    Member("member", DataObjectXapiAuthorization.class),
    /**
     * Indicates that the user must belong to the owner group for the specified project.
     *
     * @deprecated Specify the level of access for a project or other data object through {@link #Read},
     *         {@link #Edit}, or {@link #Delete} instead
     */
    @Deprecated
    Owner("owner", DataObjectXapiAuthorization.class),
    Authorizer("authorizer");

    AccessLevel(final String code) {
        this(code, null);
    }

    AccessLevel(final String code, final Class<? extends XapiAuthorization> authClass) {
        _code = code;
        _authClass = authClass;
    }

    public String code() {
        return _code;
    }

    public Class<? extends XapiAuthorization> getAuthClass() {
        return _authClass;
    }

    public boolean equals(final String value) {
        return StringUtils.equals(value, code());
    }

    public boolean equalsAny(final AccessLevel... levels) {
        return Arrays.asList(levels).contains(this);
    }

    public static AccessLevel getAccessLevel(final String code) {
        return _levels.get(code);
    }

    public static Set<String> getCodes() {
        return _levels.keySet();
    }

    private static final Map<String, AccessLevel> _levels = new HashMap<String, AccessLevel>() {{
        for (final AccessLevel level : AccessLevel.values()) {
            put(level.code(), level);
        }
    }};

    private final String                             _code;
    private final Class<? extends XapiAuthorization> _authClass;
}
