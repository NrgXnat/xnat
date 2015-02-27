package org.nrg.prefs.services.impl.hibernate;

import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.Tool;
import org.nrg.prefs.repositories.PreferenceRepository;
import org.nrg.prefs.services.PreferenceService;
import org.nrg.prefs.services.ToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Properties;

@Service
public class HibernatePreferenceService extends AbstractHibernateEntityService<Preference, PreferenceRepository> implements PreferenceService {

    @Transactional
    @Override
    public Preference getPreference(final String toolId, final String preferenceName, final Scope scope, final String entityId) {
        return getDao().findByToolIdNameAndEntity(toolId, preferenceName, scope, entityId);
    }

    @Transactional
    @Override
    public Preference getPreference(final Tool tool, final String preferenceName, final Scope scope, final String entityId) {
        return getDao().findByToolNameAndEntity(tool, preferenceName, scope, entityId);
    }

    @Transactional
    @Override
    public void setPreference(final String toolId, final String preferenceName, final Scope scope, final String entityId, final String value) {
        Preference preference = getDao().findByToolIdNameAndEntity(toolId, preferenceName, scope, entityId);
        createOrUpdatePreference(_toolService.getByToolId(toolId), preferenceName, scope, entityId, value, preference);
    }

    @Transactional
    @Override
    public void setPreference(final Tool tool, final String preferenceName, final Scope scope, final String entityId, final String value) {
        Preference preference = getDao().findByToolNameAndEntity(tool, preferenceName, scope, entityId);
        createOrUpdatePreference(tool, preferenceName, scope, entityId, value, preference);
    }

    @Transactional
    @Override
    public Properties getToolProperties(final String toolId, final Scope scope, final String entityId) {
        final List<Preference> preferences = getDao().findByToolIdAndEntity(toolId, scope, entityId);
        final Properties properties = new Properties();
        for (final Preference preference : preferences) {
            properties.setProperty(preference.getName(), preference.getValue());
        }
        return properties;
    }

    private void createOrUpdatePreference(final Tool tool, final String preferenceName, final Scope scope, final String entityId, final String value, Preference preference) {
        if (preference == null) {
            if (!tool.getToolPreferences().containsKey(preferenceName)) {
                throw new NrgServiceRuntimeException(NrgServiceError.UnsupportedFeature, "The tool " + tool.getToolId() + " doesn't support the preference " + preferenceName + ".");
            }
            if (_log.isDebugEnabled()) {
                _log.debug("Adding preference {} to tool {} with default value of {}, scope {} entity ID {}", preferenceName, tool.getToolId(), value, scope.code(), entityId);
            }
            preference = new Preference(tool, preferenceName, scope, entityId, value);
            getDao().create(preference);
        } else {
            if (_log.isDebugEnabled()) {
                _log.debug("Adding preference {} to tool {} with default value of {}, scope {} entity ID {}", preferenceName, tool.getToolId(), value, scope.code(), entityId);
            }
            preference.setValue(value);
            getDao().update(preference);
        }
    }

    @Inject
    private ToolService _toolService;

    private static final Logger _log = LoggerFactory.getLogger(HibernatePreferenceService.class);
}
