/*
 * framework: org.nrg.framework.scope.EntityResolver
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.scope;

import java.util.List;

/**
 * The entity resolver interface provides methods to resolve an entity ID to an applicable scope. Scope failover is
 * usually dependent on context: entity resolvers allow different implementations of scope failover.
 */
public interface EntityResolver<E> {
    /**
     * This method finds the hierarchy for the indicated entity ID, starting with the specified entity and working its
     * way up to the top of the object hierarchy.
     * @param entityId    The entity at which to start the hierarchy. This will always be the first entity in the
     *                    returned list.
     * @return A list of entities, starting with the entity corresponding to the indicated entity ID and going to the
     * top of the object hierarchy.
     */
    List<EntityId> getHierarchy(EntityId entityId);

    /**
     * This method resolves to the appropriate entity based on the submitted parameters. What to do with those
     * parameters is dependent on the implementation of the resolver. In some cases, the parameters may not even be
     * necessary. For example, if you're trying to find the first object in a hierarchy with an enabled property set to
     * true, there's no need to pass parameters. If the context of the resolution requires extra information, e.g. a
     * tool ID and preference name for resolving entity preferences, the implementation of this method must know what
     * types the incoming parameters are and what the values mean.
     *
     * @param entityId      The entity at which to start attempting to resolve the condition.
     * @param parameters    Any required parameters to allow the resolver to resolve the condition.
     *
     * @return The entity ID at which the condition is resolved or null if the condition is not resolved.
     */
    E resolve(EntityId entityId, Object... parameters);
}
