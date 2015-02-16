package org.nrg.prefs.scope;

import org.nrg.framework.constants.Scope;
import org.nrg.framework.scope.EntityId;

public abstract class Entity {
    public String getId() {
        return _id;
    }

    public void setId(final String id) {
        _id = id;
        _entityId = new EntityId(getScope(), _id);
    }

    public EntityId getEntityId() {
        return _entityId;
    }

    public EntityId getParentEntityId() {
        return _parentEntityId;
    }

    public void setParentEntityId(final EntityId parentEntityId) {
        _parentEntityId = parentEntityId;
    }

    abstract public Scope getScope();

    private String _id;
    private EntityId _entityId;
    private EntityId _parentEntityId;
}
