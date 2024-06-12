/*
 * framework: org.nrg.framework.scope.Wired
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.scope;

import org.nrg.framework.constants.Scope;

public abstract class Wired {
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

    public boolean isWired() {
        return _wired;
    }

    @SuppressWarnings("unused")
    public void setWired(final boolean wired) {
        _wired = wired;
    }

    abstract public Scope getScope();

    private String _id;
    private EntityId _entityId;
    private EntityId _parentEntityId;
    private boolean _wired;
}
