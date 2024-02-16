package org.nrg.xnat.compute.models;

import org.nrg.framework.constants.Scope;

import java.util.Map;
import java.util.Set;

public interface AccessScope {

    Scope getScope();

    boolean isEnabled(); // is enabled for all ids

    Set<String> getIds(); // ids that are enabled for this scope (if isEnabled is false)

    /**
     * A scope/id combination is enabled if the scope is enabled for all ids or if the id is in the set of ids
     *
     * @param id The id to check
     * @return Whether the scope/id combination is enabled
     */
    default boolean isEnabledFor(String id) {
        if (isEnabled()) {
            return true;
        } else {
            return getIds().contains(id);
        }
    }

    /**
     * Given the provided user execution scope, determine if the config is enabled for
     *
     * @param configRequiredAccessScopes The required access scopes for the config
     * @param userExecutionScope         The user's execution scope
     * @return Whether the config is enabled for the provided execution scope
     */
    static boolean isEnabledFor(Map<Scope, AccessScope> configRequiredAccessScopes, Map<Scope, String> userExecutionScope) {
        for (Scope scope : Scope.values()) {
            if (configRequiredAccessScopes.containsKey(scope)) {
                AccessScope configRequiredAccessScope = configRequiredAccessScopes.get(scope);
                if (!configRequiredAccessScope.isEnabledFor(userExecutionScope.get(scope))) {
                    return false;
                }
            }
        }
        return true;
    }

}
