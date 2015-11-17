/*
 * org.nrg.config.services.impl.DefaultConfigService
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 9/12/13 4:00 PM
 */
package org.nrg.config.services.impl;

import com.google.common.base.Joiner;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.nrg.config.daos.ConfigurationDAO;
import org.nrg.config.daos.ConfigurationDataDAO;
import org.nrg.config.entities.Configuration;
import org.nrg.config.entities.ConfigurationData;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.framework.utilities.Reflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
@Service
public class DefaultConfigService extends AbstractHibernateEntityService<Configuration, ConfigurationDAO> implements ConfigService {

    public static BeanComparator ConfigComparatorByCreateDate = new BeanComparator("created");
    public static BeanComparator ConfigComparatorByVersion = new BeanComparator("version");

    public static final boolean DEFAULT_VERSIONING = true;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        _jdbcTemplate = new JdbcTemplate(_dataSource);

        final TransactionTemplate transactionTemplate = new TransactionTemplate(_transactionManager);

        // This code converts configurations that use the deprecated project ID (which is actually the projectdata_info attribute
        // for XNAT project objects) to use the scope and entity ID instead. It also backfills the project attribute for configurations
        // created without the projectdata_info attribute. This code should be deprecated and removed eventually, maybe converted to a
        // step in a migration script.
        final TransactionCallbackWithoutResult callback = new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {
                try {
                    final Boolean testForXnatProjectTable = _jdbcTemplate.queryForObject("SELECT EXISTS ( SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'xnat_projectdata' )", Boolean.class);
                    if (testForXnatProjectTable != null && testForXnatProjectTable) {
                        final long testForSiteRefs = _jdbcTemplate.queryForObject("SELECT COUNT(*) FROM xhbm_configuration WHERE scope IS NULL AND project IS NULL", Long.class);
                        if (testForSiteRefs > 0) {
                            final int updatedSiteRefs = _jdbcTemplate.update("UPDATE xhbm_configuration SET scope = ? WHERE scope IS NULL AND project IS NULL", Scope.Site.ordinal());
                            _log.info("Updated " + updatedSiteRefs + " rows with no project or scope/entity set, set to use site scope.");
                        }
                        final long testForEntityRefs = _jdbcTemplate.queryForObject("SELECT COUNT(*) FROM xhbm_configuration WHERE scope IS NULL AND (entity_id IS NULL OR entity_id = '')", Long.class);
                        if (testForEntityRefs > 0) {
                            final int updatedEntityRefs = _jdbcTemplate.update("UPDATE xhbm_configuration SET scope = ?, entity_id = (SELECT id FROM xnat_projectdata WHERE projectdata_info = xhbm_configuration.project) WHERE scope IS NULL AND (entity_id IS NULL OR entity_id = '')", Scope.Project.ordinal());
                            _log.info("Updated " + updatedEntityRefs + " rows with no scope or entity ID set, set to use project scope, updated entity_id from join with project ID.");
                        }
                        final long testForProjectRefs = _jdbcTemplate.queryForObject("SELECT COUNT(*) FROM xhbm_configuration WHERE project IS NULL AND (entity_id IS NOT NULL AND entity_id != '')", Long.class);
                        if (testForProjectRefs > 0) {
                            final int updatedProjectRefs = _jdbcTemplate.update("UPDATE xhbm_configuration SET project = (SELECT projectdata_info FROM xnat_projectdata WHERE id = xhbm_configuration.entity_id) WHERE project IS NULL AND (entity_id IS NOT NULL AND entity_id != '')");
                            _log.info("Updated " + updatedProjectRefs + " rows with no scope or entity ID set, set to use project scope, updated entity_id from join with project ID.");
                        }
                    }
                } catch (DataAccessException exception) {
                    _log.error("There was an issue trying to recondition the configuration service data tables, rolling back all transactions", exception);
                    status.setRollbackOnly();
                }
            }
        };
        transactionTemplate.execute(callback);
    }

    @Transactional
    @Override
    public Configuration getById(long id) {
        Configuration configuration = _dao.findById(id);
        Hibernate.initialize(configuration);
        return configuration;
    }

    @Transactional
    @Override
    public List<String> getProjects() {
        return getProjects(null);
    }

    @Transactional
    @Override
    public List<String> getProjects(String toolName) {
        return _dao.getProjects(toolName);
    }

    @Transactional
    @Override
    public List<String> getTools() {
        return getToolsImpl(null, null);
    }

    @Transactional
    @Override
    @Deprecated
    public List<String> getTools(Long projectID) {
        return getToolsImpl(projectID != null ? Scope.Project : Scope.Site, getProjectIdFromLong(projectID));
    }

    @Transactional
    @Override
    public List<String> getTools(final Scope scope, final String entityId) {
        return getToolsImpl(scope, entityId);
    }

    @Transactional
    @Override
    public List<Configuration> getConfigsByTool(String toolName) {
        return getConfigsByToolImpl(toolName, null, null);
    }

    @Transactional
    @Override
    @Deprecated
    public List<Configuration> getConfigsByTool(String toolName, Long projectID) {
        return getConfigsByToolImpl(toolName, projectID != null ? Scope.Project : Scope.Site, getProjectIdFromLong(projectID));
    }

    @Transactional
    @Override
    public List<Configuration> getConfigsByTool(final String toolName, final Scope scope, final String entityId) {
        return getConfigsByToolImpl(toolName, scope, entityId);
    }

    @Transactional
    @Override
    public Configuration getConfig(String toolName, String path) {
        return getConfigImpl(toolName, path, Scope.Site, null);
    }

    @Transactional
    @Override
    @Deprecated
    public Configuration getConfig(String toolName, String path, Long projectID) {
        return getConfigImpl(toolName, path, projectID != null ? Scope.Project : Scope.Site, getProjectIdFromLong(projectID));
    }

    @Transactional
    @Override
    public Configuration getConfig(final String toolName, final String path, final Scope scope, final String entityId) {
        return getConfigImpl(toolName, path, scope, entityId);
    }

    @Transactional
    @Override
    public String getConfigContents(String toolName, String path) {
        return getConfigContentsImpl(toolName, path, Scope.Site, null);
    }

    @Transactional
    @Override
    @Deprecated
    public String getConfigContents(String toolName, String path, Long projectID) {
        return getConfigContentsImpl(toolName, path, projectID != null ? Scope.Project : Scope.Site, getProjectIdFromLong(projectID));
    }

    @Transactional
    @Override
    public String getConfigContents(final String toolName, final String path, final Scope scope, final String entityId) {
        return getConfigContentsImpl(toolName, path, scope, entityId);
    }

    @Transactional
    @Override
    @Deprecated
    public Configuration getConfigById(String toolName, String path, String id, Long projectID) {
        return getConfigByIdImpl(toolName, path, id, projectID != null ? Scope.Project : Scope.Site, getProjectIdFromLong(projectID));
    }

    @Transactional
    @Override
    public Configuration getConfigById(final String toolName, final String path, final String id, final Scope scope, final String entityId) {
        return getConfigByIdImpl(toolName, path, id, scope, entityId);
    }

    @Transactional
    @Override
    public Configuration getConfigByVersion(String toolName, String path, int version) {
        return getConfigByVersionImpl(toolName, path, version, Scope.Site, null);
    }

    @Transactional
    @Override
    @Deprecated
    public Configuration getConfigByVersion(String toolName, String path, int version, Long projectID) {
        return getConfigByVersionImpl(toolName, path, version, projectID != null ? Scope.Project : Scope.Site, getProjectIdFromLong(projectID));
    }

    @Transactional
    @Override
    public Configuration getConfigByVersion(final String toolName, final String path, final int version, final Scope scope, final String entityId) {
        return getConfigByVersionImpl(toolName, path, version, scope, entityId);
    }

    @Transactional
    @Override
    public Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents) throws ConfigServiceException {
        return replaceConfigImpl(xnatUser, reason, toolName, path, null, DEFAULT_VERSIONING, contents, Scope.Site, null);
    }

    @Transactional
    @Override
    @Deprecated
    public Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents, Long projectID) throws ConfigServiceException {
        return replaceConfigImpl(xnatUser, reason, toolName, path, null, DEFAULT_VERSIONING, contents, projectID != null ? Scope.Project : Scope.Site, getProjectIdFromLong(projectID));
    }

    @Transactional
    @Override
    public Configuration replaceConfig(final String xnatUser, final String reason, final String toolName, final String path, final String contents, final Scope scope, final String entityId) throws ConfigServiceException {
        return replaceConfigImpl(xnatUser, reason, toolName, path, null, null, contents, scope, entityId);
    }

    @Transactional
    @Override
    public Configuration replaceConfig(final String xnatUser, final String reason, final String toolName, final String path, final Boolean unversioned, final String contents) throws ConfigServiceException {
        return replaceConfigImpl(xnatUser, reason, toolName, path, null, unversioned, contents, Scope.Site, null);
    }

    @Transactional
    @Override
    @Deprecated
    public Configuration replaceConfig(final String xnatUser, final String reason, final String toolName, final String path, final Boolean unversioned, final String contents, final Long projectID) throws ConfigServiceException {
        return replaceConfigImpl(xnatUser, reason, toolName, path, null, unversioned, contents, projectID != null ? Scope.Project : Scope.Site, getProjectIdFromLong(projectID));
    }

    @Transactional
    @Override
    public Configuration replaceConfig(final String xnatUser, final String reason, final String toolName, final String path, final Boolean unversioned, final String contents, final Scope scope, final String entityId) throws ConfigServiceException {
        return replaceConfigImpl(xnatUser, reason, toolName, path, null, unversioned, contents, scope, entityId);
    }

    @Transactional
    @Override
    public String getStatus(String toolName, String path) {
        return getStatusImpl(toolName, path, Scope.Site, null);
    }

    @Transactional
    @Override
    @Deprecated
    public String getStatus(String toolName, String path, Long projectID) {
        return getStatusImpl(toolName, path, projectID != null ? Scope.Project : Scope.Site, getProjectIdFromLong(projectID));
    }

    @Transactional
    @Override
    public String getStatus(final String toolName, final String path, final Scope scope, final String entityId) {
        return getStatusImpl(toolName, path, scope, entityId);
    }

    @Transactional
    @Override
    public void enable(String xnatUser, String reason, String toolName, String path) throws ConfigServiceException {
        setStatusImpl(xnatUser, reason, toolName, path, Configuration.ENABLED_STRING, Scope.Site, null);
    }

    //fail silently if the configuration does not exist...
    @Transactional
    @Override
    @Deprecated
    public void enable(String xnatUser, String reason, String toolName, String path, Long projectID) throws ConfigServiceException {
        setStatusImpl(xnatUser, reason, toolName, path, Configuration.ENABLED_STRING, projectID != null ? Scope.Project : Scope.Site, getProjectIdFromLong(projectID));
    }

    @Transactional
    @Override
    public void enable(final String xnatUser, final String reason, final String toolName, final String path, final Scope scope, final String entityId) throws ConfigServiceException {
        setStatusImpl(xnatUser, reason, toolName, path, Configuration.ENABLED_STRING, scope, entityId);
    }

    //fail silently if the configuration does not exist...
    @Transactional
    @Override
    public void disable(String xnatUser, String reason, String toolName, String path) throws ConfigServiceException {
        setStatusImpl(xnatUser, reason, toolName, path, Configuration.DISABLED_STRING, Scope.Site, null);
    }

    //fail silently if the configuration does not exist...
    @Transactional
    @Override
    @Deprecated
    public void disable(String xnatUser, String reason, String toolName, String path, Long projectID) throws ConfigServiceException {
        setStatusImpl(xnatUser, reason, toolName, path, Configuration.DISABLED_STRING, projectID != null ? Scope.Project : Scope.Site, getProjectIdFromLong(projectID));
    }

    @Transactional
    @Override
    public void disable(final String xnatUser, final String reason, final String toolName, final String path, final Scope scope, final String entityId) throws ConfigServiceException {
        setStatusImpl(xnatUser, reason, toolName, path, Configuration.DISABLED_STRING, scope, entityId);
    }

    @Transactional
    @Override
    public List<Configuration> getHistory(String toolName, String path) {
        return getHistoryImpl(toolName, path, Scope.Site, null);
    }

    @Transactional
    @Override
    @Deprecated
    public List<Configuration> getHistory(String toolName, String path, Long projectID) {
        return getHistoryImpl(toolName, path, projectID != null ? Scope.Project : Scope.Site, getProjectIdFromLong(projectID));
    }

    @Transactional
    @Override
    public List<Configuration> getHistory(final String toolName, final String path, final Scope scope, final String entityId) {
        return getHistoryImpl(toolName, path, scope, entityId);
    }

    private List<String> getToolsImpl(Scope scope, String entityId) {
        return _dao.getTools(scope, entityId);
    }

    private List<Configuration> getConfigsByToolImpl(String toolName, Scope scope, String entityId) {
        return _dao.getConfigurationsByTool(toolName, scope, entityId);
    }

    @SuppressWarnings("unchecked")
    private Configuration getConfigImpl(String toolName, String path, Scope scope, String entityId) {
        List<Configuration> list = _dao.findByToolPathProject(toolName, path, scope, entityId);
        if (list == null || list.size() == 0) {
            return null;
        } else {
            Collections.sort(list, ConfigComparatorByCreateDate);
            return list.get(list.size() - 1);
        }
    }

    @SuppressWarnings("unchecked")
    private String getConfigContentsImpl(String toolName, String path, Scope scope, String entityId) {
        List<Configuration> list = _dao.findByToolPathProject(toolName, path, scope, entityId);
        if (list == null || list.size() == 0) {
            return null;
        } else {
            Collections.sort(list, ConfigComparatorByCreateDate);
            return list.get(list.size() - 1).getContents();
        }
    }

    private Configuration getConfigByIdImpl(String toolName, String path, String id, Scope scope, String entityId) {
        //I think it is more efficient to just pull by the ID and make sure it matches the other passed in variables.
        Configuration c = _dao.findById(Long.parseLong(id));

        //findById is silly in that it will return a non-null object even if it doesn't find a match (I didn't write it). If it didn't find a match it
        //will throw an exception the first time you try to access a property. So, we'll use that to test for a valid return, here.
        //this also takes care of a null return.
        try {
            c.getTool();
        } catch (Exception e) {
            return null;
        }
        if (StringUtils.equals(c.getTool(), toolName) && StringUtils.equals(c.getPath(), path) && (c.getScope() == scope && StringUtils.equals(entityId, c.getEntityId()))) {
            return c;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Configuration getConfigByVersionImpl(final String toolName, final String path, final int version, final Scope scope, final String entityId) {
        final List<Configuration> list = _dao.findByToolPathProject(toolName, path, scope, entityId);
        if (list == null || list.size() < version) {
            return null;
        } else {
            Collections.sort(list, ConfigComparatorByVersion);
            final Configuration ret = list.get(version - 1);
            //this will only fail if something truly stupid happened. Still should check, though.
            if (ret.getVersion() == version) {
                return ret;
            } else {
                //something odd happened, let's search the list for the version
                for (final Configuration c : list) {
                    if (c.getVersion() == version) {
                        return c;
                    }
                }
                return null;
            }
        }
    }

    private Configuration replaceConfigImpl(String xnatUser, String reason, String toolName, String path, String status, Boolean unversioned, String contents, final Scope scope, final String entityId) throws ConfigServiceException {
        if (contents != null && contents.length() > ConfigService.MAX_FILE_LENGTH) {
            throw new ConfigServiceException("file size must be less than " + ConfigService.MAX_FILE_LENGTH + " characters.");
        }

        //if a current config exists and the contents are the same as the previous version, share the config data
        Configuration oldConfig = getConfigImpl(toolName, path, scope, entityId);

        // We will version the configuration if:
        //  Case1) There is no config and unversioned is not specified (defaults to versioned) or unversioned is false
        //  Case2) There is a config, but unversioned is specified and false (parameter overrides current configuration, so we don't need  to check)
        //  Case3) NOT(There is a config, unversioned is either not specified or is true, and  the config is set to unversioned)
        boolean doVersion = (oldConfig == null && (unversioned == null || !unversioned)) || (oldConfig != null && ((unversioned != null && !unversioned) || !(oldConfig.isUnversioned())));

        Configuration configuration;
        boolean update;
        // If these things...
        if (oldConfig != null && !doVersion) {
            // We're going to update a non-versioned configuration.
            update = true;
            configuration = oldConfig;
            // If the contents have changed in a non-versioned configuration, delete the existing configuration data.
            if (configuration.getConfigData() != null && !StringUtils.equals(contents, configuration.getConfigData().getContents())) {
                _dataDAO.delete(configuration.getConfigData());
                configuration.setConfigData(null);
            }
        } else {
            // But if not those things, we're creating a new (possibly versioned or non-versioned, we don't care) configuration
            update = false;
            configuration = newEntity();
        }

        // If we have contents and there are no contents set for the configuration...
        if (configuration.getConfigData() == null) {
            // If the old configuration data doesn't differ, we'll just re-use that.
            if (oldConfig != null && oldConfig.getConfigData() != null && StringUtils.equals(contents, oldConfig.getConfigData().getContents())) {
                configuration.setConfigData(oldConfig.getConfigData());
            } else {
                // But if it doesn't, we need to create a new configuration data. For non-versioned configurations, we
                // already deleted the existing configuration data.
                ConfigurationData configurationData = new ConfigurationData();
                configurationData.setContents(contents);
                _dataDAO.create(configurationData);
                configuration.setConfigData(configurationData);
            }
        }

        configuration.setTool(toolName);
        configuration.setPath(path);
        configuration.setScope(scope);
        configuration.setEntityId(entityId);
        configuration.setXnatUser(xnatUser);
        configuration.setReason(reason);
        configuration.setStatus(StringUtils.isBlank(status) ? Configuration.ENABLED_STRING : status);
        configuration.setVersion(1 + ((oldConfig != null && doVersion) ? oldConfig.getVersion() : 0));
        configuration.setUnversioned(!doVersion);

        notifyListeners("preChange", toolName, path, configuration, true);//developers can insert pre-change logic that can prevent the storage of the configuration by throwing an exception

        if (update) {
            _dao.update(configuration);
        } else {
            _dao.create(configuration);
        }

        notifyListeners("postChange", toolName, path, configuration, false);//developers can insert post-change logic

        return configuration;
    }

    private void notifyListeners(String action, String toolName, String path, Configuration configuration, boolean ignoreExceptions) throws ConfigServiceException {
        toolName = formatForJava(toolName);
        path = formatForJava(path);

        doDynamicActions(configuration, ignoreExceptions, "org.nrg.config.extensions", action, toolName, path);
    }

    /**
     * Removes special characters and capitalizes the next character.
     *
     * @param name    The name to format.
     *
     * @return A Java-formatted string.
     */
    public static String formatForJava(String name) {
        if (StringUtils.isBlank(name)) {
            return name;
        }

        StringBuilder sb = new StringBuilder();

        name = name.replace('/', '.');
        name = name.replaceAll("[^A-Za-z0-9\\.]", "_");

        String first = name.substring(0, 1);
        sb.append(first.toUpperCase());
        name = name.substring(1).toLowerCase();

        while (name.contains("_")) {
            int i = name.indexOf("_");
            if (i + 2 > name.length()) {
                break;
            } else if (i != 0) {
                sb.append(name.substring(0, i));
                sb.append(name.substring(i + 1, i + 2).toUpperCase());
                name = name.substring(i + 2);
            } else {
                name = name.substring(1);
            }
        }
        sb.append(name);
        return sb.toString();
    }

    private void doDynamicActions(Configuration config, boolean ignoreExceptions, String... _package) throws ConfigServiceException {
        try {
            String packageTemplate = Joiner.on('.').skipNulls().join(_package);

            List<Class<?>> classes = Reflection.getClassesForPackage(packageTemplate);

            if (classes != null && classes.size() > 0) {
                for (Class<?> clazz : classes) {
                    try {
                        if (ConfigurationModificationListenerI.class.isAssignableFrom(clazz)) {
                            ConfigurationModificationListenerI action = (ConfigurationModificationListenerI) clazz.newInstance();
                            try {
                                action.execute(config);
                            } catch (ConfigServiceException e) {
                                if (ignoreExceptions) {
                                    _log.error("", e);
                                } else {
                                    throw e;
                                }
                            }
                        }
                    } catch (Exception e) {
                        _log.error("", e);
                    }
                }
            }
        } catch (Exception e) {
            _log.error("", e);
        }
    }

    public interface ConfigurationModificationListenerI {
        void execute(Configuration config) throws ConfigServiceException;
    }

    @SuppressWarnings("unchecked")
    private String getStatusImpl(String toolName, String path, Scope scope, String entityId) {
        List<Configuration> list = _dao.findByToolPathProject(toolName, path, scope, entityId);
        if (list == null || list.size() == 0) return null;
        Collections.sort(list, ConfigComparatorByCreateDate);
        return list.get(list.size() - 1).getStatus();
    }

    //fail silently if the configuration does not exist, throws an unsupported operation exception if status is null.
    @SuppressWarnings("unchecked")
    private void setStatusImpl(String xnatUser, String reason, String toolName, String path, String status, Scope scope, String entityId) throws ConfigServiceException {

        List<Configuration> list = _dao.findByToolPathProject(toolName, path, scope, entityId);
        if (list == null || list.size() == 0) {
            //fail silently if the configuration does not exist...
            return;
        }
        Collections.sort(list, ConfigComparatorByCreateDate);
        Configuration entity = list.get(list.size() - 1);

        if (!entity.isEnabled()) {
            throw new ConfigServiceException("Can't set the status on a disabled configuration.");
        }

        if (!entity.getStatus().equals(status)) {
            if (StringUtils.isBlank(reason)) {
                reason = "Setting status to " + status;
            }
            replaceConfigImpl(xnatUser, reason, toolName, path, status, entity.isUnversioned(), entity.getContents(), entity.getScope(), entity.getEntityId());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Configuration> getHistoryImpl(String toolName, String path, Scope scope, String entityId) {
        if (StringUtils.isBlank(entityId) && scope != Scope.Site) {
            throw new NrgServiceRuntimeException("You've specified scope " + scope + " without an entity ID. Scope MUST be set to Site if entity ID is blank.");
        }
        List<Configuration> list = _dao.findByToolPathProject(toolName, path, scope, entityId);
        if (list == null || list.size() == 0) return null;
        Collections.sort(list, ConfigComparatorByCreateDate);
        return list;
    }

    private String getProjectIdFromLong(final Long project) {
        final Boolean testForXnatProjectTable = _jdbcTemplate.queryForObject("SELECT EXISTS ( SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'xnat_projectdata' )", Boolean.class);
        if (testForXnatProjectTable != null && testForXnatProjectTable) {
            try {
                return _jdbcTemplate.queryForObject("SELECT id FROM xnat_projectdata WHERE projectdata_info = ?", new Object[]{project}, String.class);
            } catch (EmptyResultDataAccessException e) {
                return null;
            }
        } else {
            return project.toString();
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(DefaultConfigService.class);

    @Inject
    private ConfigurationDAO _dao;

    @Inject
    private ConfigurationDataDAO _dataDAO;

    @Inject
    private PlatformTransactionManager _transactionManager;

    @Inject
    private DataSource _dataSource;

    private JdbcTemplate _jdbcTemplate;
}
