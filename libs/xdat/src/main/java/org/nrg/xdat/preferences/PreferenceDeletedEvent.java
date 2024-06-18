/*
 * core: org.nrg.xdat.preferences.PreferenceEvent
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.preferences;

import org.nrg.framework.constants.Scope;
import org.nrg.framework.scope.EntityId;

public class PreferenceDeletedEvent extends PreferenceEvent {
    public static final String DELETED_EVENT = "<deleted>";

    public PreferenceDeletedEvent(final String preferenceName) {
        super(preferenceName, DELETED_EVENT);
    }
    public PreferenceDeletedEvent(final EntityId entityId, final String preferenceName) {
        super(entityId, preferenceName, DELETED_EVENT);
    }

    public PreferenceDeletedEvent(final Scope scope, final String entityId, final String preferenceName) {
        super(scope, entityId, preferenceName, DELETED_EVENT);
    }
}
