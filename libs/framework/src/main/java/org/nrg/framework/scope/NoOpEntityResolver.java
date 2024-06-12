/*
 * framework: org.nrg.framework.scope.NoOpEntityResolver
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.scope;

import java.util.Collections;
import java.util.List;

/**
 * The most basic entity resolver. This will always resolve to a single item hierarchy and returns some default value
 * when resolving.
 */
public class NoOpEntityResolver implements EntityResolver<Object> {
    /**
     * Returns a hierarchy of one.
     * @param entityId    The entity at which to start the hierarchy. This will always be the first entity in the
     *                    returned list.
     * @return A list containing only the submitted entity ID.
     */
    @Override
    public List<EntityId> getHierarchy(final EntityId entityId) {
        return Collections.singletonList(entityId);
    }

    /**
     * Resolves the submitted entity ID to either the first item in the array of parameters or, if no parameters are
     * submitted, the entity ID itself. Note that this is almost never useful but exists primarily to fulfill the need
     * for an entity resolver in situations where entities don't really need to be resolved, e.g. when all entities are
     * presumed to be at a default scope.
     *
     * @param entityId      The entity at which to start attempting to resolve the condition.
     * @param parameters    Any required parameters to allow the resolver to resolve the condition.
     *
     * @return The first item in the array of parameters or, if no parameters are submitted, the entity ID itself.
     */
    @Override
    public Object resolve(final EntityId entityId, final Object... parameters) {
        return (parameters == null || parameters.length == 0) ? entityId : parameters[0];
    }
}
