/*
 * prefs: org.nrg.prefs.services.impl.hibernate.HibernatePreferenceService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.services.impl.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.framework.scope.EntityId;
import org.nrg.framework.utilities.Reflection;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.PreferenceBean;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.PreferenceInfo;
import org.nrg.prefs.entities.Tool;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.repositories.PreferenceRepository;
import org.nrg.prefs.services.PreferenceService;
import org.nrg.prefs.services.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
@Slf4j
public class HibernatePreferenceService extends AbstractHibernateEntityService<Preference, PreferenceRepository> implements PreferenceService {
    @Autowired
    public HibernatePreferenceService(final ToolService toolService) {
        _toolService = toolService;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public boolean hasPreference(final String toolId, final String preference) {
        return hasPreference(toolId, preference, EntityId.Default.getScope(), EntityId.Default.getEntityId());
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public boolean hasPreference(final String toolId, final String preference, final Scope scope, final String entityId) {
        return getDao().findByToolIdNameAndEntity(toolId, preference, scope, entityId) != null;
    }

    @Transactional
    @Override
    public Preference getPreference(final String toolId, final String preferenceName) {
        return getPreference(toolId, preferenceName, Scope.Site, null);
    }

    @Transactional
    @Override
    public Preference getPreference(final String toolId, final String preferenceName, final Scope scope, final String entityId) {
        return getDao().findByToolIdNameAndEntity(toolId, preferenceName, scope, resolveEntityId(entityId));
    }

    @Transactional
    @Override
    public void setPreference(final String toolId, final String preferenceName, final String value) throws InvalidPreferenceName {
        setPreference(toolId, preferenceName, Scope.Site, null, value);
    }

    @Transactional
    @Override
    public void setPreference(final String toolId, final String preferenceName, final Scope scope, final String entityId, final String value) throws InvalidPreferenceName {
        final String     resolvedEntityId = resolveEntityId(entityId);
        final Preference preference       = getDao().findByToolIdNameAndEntity(toolId, preferenceName, scope, resolvedEntityId);
        createOrUpdatePreference(_toolService.getByToolId(toolId), preferenceName, scope, resolvedEntityId, value, preference);
    }

    @Transactional
    @Override
    public void delete(final String toolId, final String preferenceName) throws InvalidPreferenceName {
        delete(toolId, preferenceName, Scope.Site, null);
    }

    @Transactional
    @Override
    public void delete(final String toolId, final String preferenceName, final Scope scope, final String entityId) throws InvalidPreferenceName {
        final String     resolvedEntityId = resolveEntityId(entityId);
        final Preference preference       = getDao().findByToolIdNameAndEntity(toolId, preferenceName, scope, resolvedEntityId);
        if (preference == null) {
            throw new InvalidPreferenceName("Couldn't find the preference " + preferenceName + " for the tool " + toolId + (StringUtils.isNotBlank(resolvedEntityId) ? " and entity " + scope + ":" + resolvedEntityId : ""));
        }
        getDao().delete(preference);
    }

    @Transactional
    @Override
    public Properties getToolProperties(final String toolId, final Scope scope, final String entityId) {
        return getToolProperties(toolId, scope, entityId, null);
    }

    @Transactional
    @Override
    public Properties getToolProperties(final String toolId, final Scope scope, final String entityId, final List<String> preferenceNames) {
        final boolean          unfiltered       = preferenceNames == null || preferenceNames.size() == 0;
        final String           resolvedEntityId = resolveEntityId(entityId);
        final List<Preference> preferences      = getDao().findByToolIdAndEntity(toolId, scope, resolvedEntityId);
        final Properties       properties       = new Properties();

        for (final Preference preference : preferences) {
            /*
                TODO: Something weird happens here: after deleting a preference, when you retrieve prefs by toolId, you
                get a null entry in the list where the deleted pref used to be, so you have to check and skip it. Root
                cause of this needs to be found, but this is a hacky workaround in the meantime.
            */
            if (preference != null && (unfiltered || preferenceNames.contains(StringUtils.substringBefore(preference.getName(), ":")))) {
                properties.setProperty(preference.getName(), preference.getValue());
            }
        }
        return properties;
    }

    private void createOrUpdatePreference(final Tool tool, final String preferenceName, final Scope scope, final String entityId, final String value, Preference preference) throws InvalidPreferenceName {
        final String resolvedEntityId = resolveEntityId(entityId);
        if (preference == null) {
            if (tool.isStrict() && !isValidPreference(tool, preferenceName)) {
                throw new InvalidPreferenceName("The tool " + tool.getToolId() + " doesn't support the preference " + preferenceName + " and is set to use a strict preferences list.");
            }
            log.debug("Adding preference {} to tool {} with default value of {}, scope {} entity ID {}", preferenceName, tool.getToolId(), value, scope.code(), resolvedEntityId);
            preference = new Preference(tool, preferenceName, scope, resolvedEntityId, value);
            getDao().create(preference);
        } else {
            log.debug("Adding preference {} to tool {} with default value of {}, scope {} entity ID {}", preferenceName, tool.getToolId(), value, scope.code(), resolvedEntityId);
            preference.setValue(value);
            getDao().update(preference);
        }
    }

    private static String getPreferenceKey(final String preferenceName) {
        return StringUtils.isBlank(preferenceName) ? "" : preferenceName.split(PreferenceBean.NAMESPACE_DELIMITER, 2)[0];
    }

    private static String resolveEntityId(final String entityId) {
        return StringUtils.isBlank(entityId) ? EntityId.Default.getEntityId() : entityId;
    }

    private boolean isValidPreference(final Tool tool, final String preferenceName) {
        // TODO: Should maybe throw an exception when beans don't include tool ID, but this may be a valid situation?
        final String toolId = tool.getToolId();
        if (!getBeansById().containsKey(toolId)) {
            log.info("Checked for the preference setting {} in a non-existent tool {}.", preferenceName, toolId);
            return false;
        }
        final Map<String, PreferenceInfo> defaultPreferences = getBeansById().get(toolId).getDefaultPreferences();
        return defaultPreferences.containsKey(preferenceName) || defaultPreferences.containsKey(getPreferenceKey(preferenceName));
    }

    private Map<String, PreferenceBean> getBeansById() {
        if (_beansById.size() == 0) {
            final Map<String, PreferenceBean> beans = getContext().getBeansOfType(PreferenceBean.class);
            for (final PreferenceBean bean : beans.values()) {
                final Class<? extends PreferenceBean> clazz      = bean.getClass();
                final NrgPreferenceBean               annotation = Reflection.findAnnotationInClassHierarchy(clazz, NrgPreferenceBean.class);
                if (annotation != null) {
                    final String toolId = annotation.toolId();
                    _beansById.put(toolId, bean);
                }
            }
        }
        return _beansById;
    }

    private final ToolService _toolService;

    private final Map<String, PreferenceBean> _beansById = new HashMap<>();
}
