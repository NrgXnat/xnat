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

public class PreferenceCreatedEvent extends PreferenceEvent {
    public PreferenceCreatedEvent(final String preferenceName, final String value) {
        super(preferenceName, value);
    }

    public PreferenceCreatedEvent(final EntityId entityId, final String preferenceName, final String value) {
        super(entityId, preferenceName, value);
    }

    public PreferenceCreatedEvent(final Scope scope, final String entityId, final String preferenceName, final String value) {
        super(scope, entityId, preferenceName, value);
    }
}
