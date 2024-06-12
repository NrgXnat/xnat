/*
 * framework: org.nrg.framework.constants.Scope
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public enum Scope {
    Site("site"),
    Project("prj"),
    Subject("subj"),
    Experiment("experiment"),
    User("user"),
    DataType("datatype");

    Scope(final String code) {
        _code = code;
    }

    public String code() {
        return _code;
    }

    public static Scope getDefaultScope() {
        return Site;
    }

    public static Scope getScope(final String code) {
        if (_scopes.isEmpty()) {
            synchronized (Scope.class) {
                for (Scope scope : values()) {
                    _scopes.put(scope.code(), scope);
                }
            }
        }
        return _scopes.get(code);
    }

    public static Set<String> getCodes() {
        if (_scopes.isEmpty()) {
            getScope("site");
        }
        return _scopes.keySet();
    }

    public static String encode(final Scope scope, final String entityId) {
        if (scope == Site) {
            return Site.code();
        }
        return String.format("%s:%s", scope.code(), entityId);
    }

    public static Map<String, String> decode(final String association) {
        final String[] atoms = association.split(":", 2);
        final Scope scope = Scope.getScope(atoms[0]);
        final String entityId = atoms.length == 1 ? null : atoms[1];
        Map<String, String> items = new HashMap<>();
        items.put("scope", scope.code());
        items.put("entityId", entityId);
        return items;
    }

    private static final Map<String, Scope> _scopes = new ConcurrentHashMap<>();
    private final String _code;
}
