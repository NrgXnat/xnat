package org.nrg.prefs.services.impl.hibernate;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.framework.scope.EntityId;
import org.nrg.prefs.beans.PreferenceBean;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.Tool;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.repositories.PreferenceRepository;
import org.nrg.prefs.services.PreferenceService;
import org.nrg.prefs.services.ToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;

@Service
public class HibernatePreferenceService extends AbstractHibernateEntityService<Preference, PreferenceRepository> implements PreferenceService {

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
        final Preference preference = getDao().findByToolIdNameAndEntity(toolId, preferenceName, scope, resolvedEntityId);
        if (preference == null) {
            throw new InvalidPreferenceName("Couldn't find the preference " + preferenceName + " for the tool " + toolId + (StringUtils.isNotBlank(resolvedEntityId) ? " and entity " + scope + ":" + resolvedEntityId : ""));
        }
        getDao().delete(preference);
    }

    @Transactional
    @Override
    public Properties getToolProperties(final String toolId, final Scope scope, final String entityId) {
        final String     resolvedEntityId = resolveEntityId(entityId);
        final List<Preference> preferences = getDao().findByToolIdAndEntity(toolId, scope, resolvedEntityId);
        final Properties       properties  = new Properties();
        for (final Preference preference : preferences) {
            properties.setProperty(preference.getName(), preference.getValue());
        }
        return properties;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        if (_preferenceBeans != null) {
            for (final PreferenceBean bean : _preferenceBeans) {
                _beansById.put(bean.getToolId(), bean);
            }
        } else {
            _preferenceBeans = new ArrayList<>();
        }
    }

    private void createOrUpdatePreference(final Tool tool, final String preferenceName, final Scope scope, final String entityId, final String value, Preference preference) throws InvalidPreferenceName {
        final String resolvedEntityId = resolveEntityId(entityId);
        if (preference == null) {
            if (tool.isStrict() && !_beansById.get(tool.getToolId()).getDefaultPreferences().containsKey(preferenceName) && !isValidPreference(tool, preferenceName)) {
                throw new InvalidPreferenceName("The tool " + tool.getToolId() + " doesn't support the preference " + preferenceName + " and is set to use a strict preferences list.");
            }
            if (_log.isDebugEnabled()) {
                _log.debug("Adding preference {} to tool {} with default value of {}, scope {} entity ID {}", preferenceName, tool.getToolId(), value, scope.code(), resolvedEntityId);
            }
            preference = new Preference(tool, preferenceName, scope, resolvedEntityId, value);
            getDao().create(preference);
        } else {
            if (_log.isDebugEnabled()) {
                _log.debug("Adding preference {} to tool {} with default value of {}, scope {} entity ID {}", preferenceName, tool.getToolId(), value, scope.code(), resolvedEntityId);
            }
            preference.setValue(value);
            getDao().update(preference);
        }
    }

    private boolean isValidPreference(final Tool tool, final String preferenceName) {
        // TODO: Should maybe throw an exception when beans don't include tool ID, but this may be a valid situation?
        return _beansById.containsKey(tool.getToolId()) && _beansById.get(tool.getToolId()).getDefaultPreferences().containsKey(preferenceName);
    }

    private static String resolveEntityId(final String entityId) {
        return StringUtils.isBlank(entityId) ? EntityId.Default.getEntityId() : entityId;
    }

    private static final Logger _log = LoggerFactory.getLogger(HibernatePreferenceService.class);

    @Inject
    private ToolService _toolService;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Autowired(required = false)
    private List<PreferenceBean> _preferenceBeans;

    private Map<String, PreferenceBean> _beansById = new HashMap<>();
}
