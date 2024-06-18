/*
 * core: org.nrg.xdat.preferences.PreferenceEvent
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.preferences;

import com.google.common.collect.ImmutableMap;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.event.StructuredEvent;
import org.nrg.framework.event.entities.EventSpecificFields;
import org.nrg.framework.scope.EntityId;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PreferenceEvent extends StructuredEvent {
    public PreferenceEvent(final String preferenceName, final String value) {
        this(null, null, ImmutableMap.of(preferenceName, value));
    }

    public PreferenceEvent(final Map<String, String> preferences) {
        this(null, null, preferences);
    }

    public PreferenceEvent(final EntityId entityId, final String preferenceName, final String value) {
        this(entityId.getScope(), entityId.getEntityId(), ImmutableMap.of(preferenceName, value));
    }

    @SuppressWarnings("unused")
    public PreferenceEvent(final EntityId entityId, final Map<String, String> preferences) {
        this(entityId.getScope(), entityId.getEntityId(), preferences);
    }

    public PreferenceEvent(final Scope scope, final String entityId, final String preferenceName, final String value) {
        this(scope, entityId, ImmutableMap.of(preferenceName, value));
    }

    public PreferenceEvent(final Scope scope, final String entityId, final Map<String, String> preferences) {
        if (scope != null) {
            setEntityType(scope.code());
        }
        if (entityId != null) {
            setEntityId(entityId);
        }

        final Set<EventSpecificFields> fields = new HashSet<>();
        for (final String preference : preferences.keySet()) {
            fields.add(new EventSpecificFields(preference, preferences.get(preference)));
        }
        setEventSpecificFields(fields);
    }
}
