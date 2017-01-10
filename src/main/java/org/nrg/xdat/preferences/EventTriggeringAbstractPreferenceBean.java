/*
 * core: org.nrg.xdat.preferences.EventTriggeringAbstractPreferenceBean
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.preferences;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.scope.EntityId;
import org.nrg.framework.services.NrgEventService;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.exceptions.UnknownToolId;
import org.nrg.prefs.services.NrgPreferenceService;

import java.nio.file.Path;
import java.util.List;
import org.nrg.framework.configuration.ConfigPaths;

public abstract class EventTriggeringAbstractPreferenceBean extends AbstractPreferenceBean {
    protected EventTriggeringAbstractPreferenceBean(final NrgPreferenceService preferenceService, final NrgEventService eventService) {
        this(preferenceService,eventService,null);
    }

    protected EventTriggeringAbstractPreferenceBean(final NrgPreferenceService preferenceService, final NrgEventService eventService, final ConfigPaths configFolderPaths) {
        super(preferenceService,configFolderPaths);
        _eventService = eventService;
    }

    @JsonIgnore
    @Override
    public String set(final String value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        return set(EntityId.Default.getScope(), EntityId.Default.getEntityId(), value, key, subkeys);
    }

    @JsonIgnore
    @Override
    public String set(final Scope scope, final String entityId, final String value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        final String namespacedPropertyId = getNamespacedPropertyId(key, subkeys);
        final String current = super.set(scope, entityId, value, key, subkeys);
        triggerEventIfChanging(namespacedPropertyId, current, value);
        return current;
    }

    private void triggerEventIfChanging(final String namespacedPropertyId, final String oldValue, final String newValue){
        if(!StringUtils.equals(oldValue,newValue)) { //Check if value is being changed.
            _eventService.triggerEvent(new PreferenceEvent(namespacedPropertyId, newValue));
        }
    }

    private final NrgEventService _eventService;
}
