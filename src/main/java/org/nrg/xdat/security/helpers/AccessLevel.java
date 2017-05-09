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
import org.nrg.xapi.rest.XapiRequestMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Defines the access levels available for various XNAT resources. These can be used with the {@link
 * XapiRequestMapping#restrictTo()} attribute and other permissions operations.
 */
public enum AccessLevel {
    Null(null),
    Admin("admin"),
    Authenticated("authenticated"),
    Read("read"),
    Edit("edit"),
    Collaborator("collaborator"),
    Member("member"),
    Owner("owner");

    AccessLevel(final String code) {
        _code = code;
    }

    public String code() {
        return _code;
    }

    public boolean equals(final String value) {
        return StringUtils.equals(value, code());
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

    private final String _code;
}
