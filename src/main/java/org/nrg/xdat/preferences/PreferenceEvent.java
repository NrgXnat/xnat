/*
 * core: org.nrg.xdat.preferences.PreferenceEvent
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.preferences;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.event.StructuredEvent;
import org.nrg.framework.event.entities.EventSpecificFields;
import org.nrg.framework.scope.EntityId;

public class PreferenceEvent extends StructuredEvent {
    public PreferenceEvent(final String preferenceName, final String value) {
        this(null, null, preferenceName, value);
    }

    public PreferenceEvent(final EntityId entityId, final String preferenceName, final String value) {
        this(entityId.getScope(), entityId.getEntityId(), preferenceName, value);
    }

    public PreferenceEvent(final Scope scope, final String entityId, final String preferenceName, final String value) {
        if (scope != null) {
            setEntityType(scope.code());
        }
        if (entityId != null) {
            setEntityId(entityId);
        }
        setEventSpecificFields(Sets.newHashSet(new EventSpecificFields(preferenceName, value)));
    }
}
