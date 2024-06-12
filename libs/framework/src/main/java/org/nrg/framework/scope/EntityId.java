/*
 * framework: org.nrg.framework.scope.EntityId
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.scope;

import org.nrg.framework.constants.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Provides properties that can be used to uniquely identify an object in the system. The meaning of the entity ID
 * attribute is dependent on the definition of the entity scope. That is, the entity ID identifies an object uniquely
 * within the defined scope.
 */
public class EntityId implements Serializable {
    public EntityId() {
        _log.debug("Creating default entity ID");
        _scope = Scope.getDefaultScope();
        _entityId = "";
    }

    public EntityId(final Scope scope, final String entityId) {
        if (_log.isDebugEnabled()) {
            _log.debug("Creating entity ID with scope " + scope.toString() + " and ID " + entityId);
        }
        _scope = scope;
        _entityId = entityId;
    }

    public Scope getScope() {
        return _scope;
    }

    public String getEntityId() {
        return _entityId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EntityId)) {
            return false;
        }

        final EntityId entityId = (EntityId) o;

        return !(_entityId != null ? !_entityId.equals(entityId._entityId) : entityId._entityId != null) && _scope == entityId._scope;

    }

    @Override
    public int hashCode() {
        int result = _scope.hashCode();
        result = 31 * result + (_entityId != null ? _entityId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return toString(_scope, _entityId);
    }

    public static String toString(final Scope scope, final String entityId) {
        return "Entity ID: " + scope.toString() + "[" + entityId + "]";
    }

    private static final Logger _log = LoggerFactory.getLogger(EntityId.class);

    public static final EntityId Default = new EntityId();
    private final Scope _scope;
    private final String _entityId;
}
