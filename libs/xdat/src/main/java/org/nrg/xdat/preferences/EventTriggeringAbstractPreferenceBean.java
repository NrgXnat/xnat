/*
 * core: org.nrg.xdat.preferences.EventTriggeringAbstractPreferenceBean
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.scope.EntityId;
import org.nrg.framework.services.NrgEventService;
import org.nrg.framework.services.NrgEventServiceI;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.exceptions.UnknownToolId;
import org.nrg.prefs.services.NrgPreferenceService;

public abstract class EventTriggeringAbstractPreferenceBean extends AbstractPreferenceBean {
    /**
     * Creates the bean instance with the submitted preference and event services.
     *
     * @param preferenceService The {@link NrgPreferenceService preference service instance}.
     * @param eventService      The {@link NrgEventService event service instance}.
     *
     * @deprecated Use the {@link #EventTriggeringAbstractPreferenceBean(NrgPreferenceService, NrgEventServiceI)} constructor instead.
     */
    @Deprecated
    protected EventTriggeringAbstractPreferenceBean(final NrgPreferenceService preferenceService, final NrgEventService eventService) {
        this(preferenceService, eventService, null);
    }

    /**
     * Creates the bean instance with the submitted preference and event services.
     *
     * @param preferenceService The {@link NrgPreferenceService preference service instance}.
     * @param eventService      The {@link NrgEventServiceI event service instance}.
     *
     * @deprecated You should always use the {@link #EventTriggeringAbstractPreferenceBean(NrgPreferenceService, NrgEventServiceI, ConfigPaths, OrderedProperties)} version
     *     of this constructor to allow initialization and override preference settings to be configured.
     */
    @Deprecated
    protected EventTriggeringAbstractPreferenceBean(final NrgPreferenceService preferenceService, final NrgEventServiceI eventService) {
        this(preferenceService, eventService, null);
    }

    /**
     * Creates the bean instance with the submitted preference and event services.
     *
     * @param preferenceService The {@link NrgPreferenceService preference service instance}.
     * @param eventService      The {@link NrgEventService event service instance}.
     * @param configPaths       The {@link ConfigPaths configuration paths instance}.
     *
     * @deprecated Use the {@link #EventTriggeringAbstractPreferenceBean(NrgPreferenceService, NrgEventServiceI, ConfigPaths)} constructor instead.
     */
    @Deprecated
    protected EventTriggeringAbstractPreferenceBean(final NrgPreferenceService preferenceService, final NrgEventService eventService, final ConfigPaths configPaths) {
        this(preferenceService, eventService, configPaths, null);
    }

    /**
     * Creates the bean instance with the submitted preference and event services, getting its configuration settings from the configuration files on the {@link ConfigPaths configuration paths}.
     *
     * @param preferenceService The {@link NrgPreferenceService preference service instance}.
     * @param eventService      The {@link NrgEventServiceI event service instance}.
     * @param configPaths       The {@link ConfigPaths configuration paths instance}.
     *
     * @deprecated You should always use the {@link #EventTriggeringAbstractPreferenceBean(NrgPreferenceService, NrgEventServiceI, ConfigPaths, OrderedProperties)} version
     *     of this constructor to allow initialization and override preference settings to be configured.
     */
    @Deprecated
    protected EventTriggeringAbstractPreferenceBean(final NrgPreferenceService preferenceService, final NrgEventServiceI eventService, final ConfigPaths configPaths) {
        this(preferenceService, eventService, configPaths, null);
    }

    /**
     * Creates the bean instance with the submitted preference and event services.
     *
     * @param preferenceService The {@link NrgPreferenceService preference service instance}.
     * @param eventService      The {@link NrgEventService event service instance}.
     * @param configPaths       The {@link ConfigPaths configuration paths instance}.
     * @param initPrefs         The initialization preferences.
     *
     * @deprecated Use the {@link #EventTriggeringAbstractPreferenceBean(NrgPreferenceService, NrgEventServiceI, ConfigPaths, OrderedProperties)} constructor instead.
     */
    @Deprecated
    protected EventTriggeringAbstractPreferenceBean(final NrgPreferenceService preferenceService, final NrgEventService eventService, final ConfigPaths configPaths, final OrderedProperties initPrefs) {
        this(preferenceService, (NrgEventServiceI) eventService, configPaths, initPrefs);
    }

    /**
     * Creates the bean instance with the submitted preference and event services, getting its configuration settings from the initialization and configuration files on the {@link ConfigPaths
     * configuration paths}.
     *
     * @param preferenceService The {@link NrgPreferenceService preference service instance}.
     * @param eventService      The {@link NrgEventServiceI event service instance}.
     * @param configPaths       The {@link ConfigPaths configuration paths instance}.
     * @param initPrefs         The initialization preferences.
     */
    protected EventTriggeringAbstractPreferenceBean(final NrgPreferenceService preferenceService, final NrgEventServiceI eventService, final ConfigPaths configPaths, final OrderedProperties initPrefs) {
        super(preferenceService, configPaths, initPrefs);
        _eventService = eventService;
    }

    @Override
    public void create(final String value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        final String namespacedPropertyId = getNamespacedPropertyId(key, subkeys);
        super.create(value, key, subkeys);
        triggerCreatedEvent(namespacedPropertyId, value);
    }

    @Override
    public void create(final Scope scope, final String entityId, final String value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        final String namespacedPropertyId = getNamespacedPropertyId(key, subkeys);
        super.create(scope, entityId, value, key, subkeys);
        triggerCreatedEvent(scope, entityId, namespacedPropertyId, value);
    }

    @Override
    public String set(final String value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        return set(EntityId.Default.getScope(), EntityId.Default.getEntityId(), value, key, subkeys);
    }

    @Override
    public String set(final Scope scope, final String entityId, final String value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        final String namespacedPropertyId = getNamespacedPropertyId(key, subkeys);
        final String current              = super.set(scope, entityId, value, key, subkeys);
        if (scope == null || scope == EntityId.Default.getScope()) {
            triggerEventIfChanging(namespacedPropertyId, current, value);
        } else {
            triggerEventIfChanging(scope, entityId, namespacedPropertyId, current, value);
        }
        return current;
    }

    @Override
    public void delete(final String key, final String... subkeys) throws InvalidPreferenceName {
        final String namespacedPropertyId = getNamespacedPropertyId(key, subkeys);
        super.delete(key, subkeys);
        triggerDeletedEvent(namespacedPropertyId);
    }

    @Override
    public void delete(final Scope scope, final String entityId, final String key, final String... subkeys) throws InvalidPreferenceName {
        final String namespacedPropertyId = getNamespacedPropertyId(key, subkeys);
        super.delete(key, subkeys);
        triggerDeletedEvent(scope, entityId, namespacedPropertyId);
    }

    @SuppressWarnings("UnusedReturnValue")
    public List<String> setBatch(final Map<String, String> values) throws UnknownToolId, InvalidPreferenceName {
        return setBatch(EntityId.Default.getScope(), EntityId.Default.getEntityId(), values);
    }

    public List<String> setBatch(final Scope scope, final String entityId, Map<String, String> values) throws UnknownToolId, InvalidPreferenceName {
        final List<String>  currentValues = new ArrayList<>();
        final AtomicBoolean isModified    = new AtomicBoolean();
        for (final String preferenceId : values.keySet()) {
            final String value   = values.get(preferenceId);
            final String current = super.set(scope, entityId, value, preferenceId);
            currentValues.add(current);
            if (!StringUtils.equals(current, value)) {
                isModified.set(true);
            }
        }
        if (isModified.get()) {
            final PreferenceEvent preferenceEvent;
            if (scope == null || scope == EntityId.Default.getScope()) {
                preferenceEvent = new PreferenceEvent(values);
            } else {
                preferenceEvent = new PreferenceEvent(scope, entityId, values);
            }
            _eventService.triggerEvent(preferenceEvent);
        }
        return currentValues;
    }

    private void triggerCreatedEvent(final String namespacedPropertyId, final String value) {
        _eventService.triggerEvent(new PreferenceCreatedEvent(namespacedPropertyId, value));
    }

    private void triggerCreatedEvent(final Scope scope, final String entityId, final String namespacedPropertyId, final String value) {
        _eventService.triggerEvent(new PreferenceCreatedEvent(scope, entityId, namespacedPropertyId, value));
    }

    private void triggerEventIfChanging(final String namespacedPropertyId, final String oldValue, final String newValue) {
        if (!StringUtils.equals(oldValue, newValue)) { //Check if value is being changed.
            _eventService.triggerEvent(new PreferenceEvent(namespacedPropertyId, newValue));
        }
    }

    private void triggerEventIfChanging(final Scope scope, final String entityId, final String namespacedPropertyId, final String oldValue, final String newValue) {
        if (!StringUtils.equals(oldValue, newValue)) { //Check if value is being changed.
            _eventService.triggerEvent(new PreferenceEvent(scope, entityId, namespacedPropertyId, newValue));
        }
    }

    private void triggerDeletedEvent(final String namespacedPropertyId) {
        _eventService.triggerEvent(new PreferenceDeletedEvent(namespacedPropertyId));
    }

    private void triggerDeletedEvent(final Scope scope, final String entityId, final String namespacedPropertyId) {
        _eventService.triggerEvent(new PreferenceDeletedEvent(scope, entityId, namespacedPropertyId));
    }

    private final NrgEventServiceI _eventService;
}
