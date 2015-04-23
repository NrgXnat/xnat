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

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.nrg.config.daos.ConfigurationDAO;
import org.nrg.config.daos.ConfigurationDataDAO;
import org.nrg.config.entities.Configuration;
import org.nrg.config.entities.ConfigurationData;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

@Service
public class DefaultConfigService extends AbstractHibernateEntityService<Configuration, ConfigurationDAO> implements ConfigService {

    public static BeanComparator ConfigComparatorByCreateDate = new BeanComparator("created");
    public static BeanComparator ConfigComparatorByVersion = new BeanComparator("version");

    @Transactional
    @Override
    public Configuration getById(long id) {
        Configuration configuration = _dao.findById(id);
        Hibernate.initialize(configuration);
        return configuration;
    }

    @Transactional
    @Override
    public List<Long> getProjects() {
        return getProjects(null);
    }

    @Transactional
    @Override
    public List<Long> getProjects(String toolName) {
        return _dao.getProjects(toolName);
    }

    @Transactional
    @Override
    public List<String> getTools() {
        return getToolsImpl(null);
    }

    @Transactional
    @Override
    public List<String> getTools(Long projectID) {
        return getToolsImpl(projectID);
    }

    @Override
    public List<String> getTools(final Scope scope, final String entityId) {
        return getToolsImpl(scope, entityId);
    }

    @Transactional
    @Override
    public List<Configuration> getConfigsByTool(String toolName) {
        return getConfigsByToolImpl(toolName, null);
    }

    @Transactional
    @Override
    public List<Configuration> getConfigsByTool(String toolName, Long projectID) {
        return getConfigsByToolImpl(toolName, projectID);
    }

    @Override
    public List<Configuration> getConfigsByTool(final String toolName, final Scope scope, final String entityId) {
        return getConfigsByToolImpl(toolName, scope, entityId);
    }

    @Transactional
    @Override
    public Configuration getConfig(String toolName, String path) {
        return getConfigImpl(toolName, path, null);
    }

    @Transactional
    @Override
    public Configuration getConfig(String toolName, String path, Long projectID) {
        return getConfigImpl(toolName, path, projectID);
    }

    @Transactional
    @Override
    public Configuration getConfig(final String toolName, final String path, final Scope scope, final String entityId) {
        return getConfigImpl(toolName, path, scope, entityId);
    }

    @Transactional
    @Override
    public String getConfigContents(String toolName, String path) {
        return getConfigContentsImpl(toolName, path, null);
    }

    @Transactional
    @Override
    public String getConfigContents(String toolName, String path, Long projectID) {
        return getConfigContentsImpl(toolName, path, projectID);
    }

    @Transactional
    @Override
    public String getConfigContents(final String toolName, final String path, final Scope scope, final String entityId) {
        return getConfigContentsImpl(toolName, path, scope, entityId);
    }

    @Transactional
    @Override
    public Configuration getConfigById(String toolName, String path, String id) {
        return getConfigByIdImpl(toolName, path, id, null, null, null);
    }

    @Transactional
    @Override
    public Configuration getConfigById(String toolName, String path, String id, Long projectID) {
        return getConfigByIdImpl(toolName, path, id, projectID, null, null);
    }

    @Transactional
    @Override
    public Configuration getConfigById(final String toolName, final String path, final String id, final Scope scope, final String entityId) {
        return getConfigByIdImpl(toolName, path, id, null, scope, entityId);
    }

    @Transactional
    @Override
    public Configuration getConfigByVersion(String toolName, String path, int version) {
        return getConfigByVersionImpl(toolName, path, version, null);
    }

    @Transactional
    @Override
    public Configuration getConfigByVersion(String toolName, String path, int version, Long projectID) {
        return getConfigByVersionImpl(toolName, path, version, projectID);
    }

    @Transactional
    @Override
    public Configuration getConfigByVersion(final String toolName, final String path, final int version, final Scope scope, final String entityId) {
        return getConfigByVersionImpl(toolName, path, version, scope, entityId);
    }

    @Transactional
    @Override
    public Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents) throws ConfigServiceException {
        return replaceConfig(xnatUser, reason, toolName, path, null, contents);
    }

    @Transactional
    @Override
    public Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents, Long projectID) throws ConfigServiceException {
        return replaceConfig(xnatUser, reason, toolName, path, null, contents, projectID);
    }

    @Transactional
    @Override
    public Configuration replaceConfig(final String xnatUser, final String reason, final String toolName, final String path, final String contents, final Scope scope, final String entityId) throws ConfigServiceException {
        return replaceConfigImpl(xnatUser, reason, toolName, path, null, null, contents, null, scope, entityId);
    }

    @Transactional
    @Override
    public Configuration replaceConfig(final String xnatUser, final String reason, final String toolName, final String path, final Boolean unversioned, final String contents) throws ConfigServiceException {
        return replaceConfigImpl(xnatUser, reason, toolName, path, null, unversioned, contents, null, null, null);
    }

    @Transactional
    @Override
    public Configuration replaceConfig(final String xnatUser, final String reason, final String toolName, final String path, final Boolean unversioned, final String contents, final Long projectID) throws ConfigServiceException {
        return replaceConfigImpl(xnatUser, reason, toolName, path, null, unversioned, contents, projectID, null, null);
    }

    @Transactional
    @Override
    public Configuration replaceConfig(final String xnatUser, final String reason, final String toolName, final String path, final Boolean unversioned, final String contents, final Scope scope, final String entityId) throws ConfigServiceException {
        return replaceConfigImpl(xnatUser, reason, toolName, path, null, unversioned, contents, null, scope, entityId);
    }

    @Transactional
    @Override
    public String getStatus(String toolName, String path) {
        return getStatusImpl(toolName, path, null);
    }

    @Transactional
    @Override
    public String getStatus(String toolName, String path, Long projectID) {
        return getStatusImpl(toolName, path, projectID);
    }

    @Transactional
    @Override
    public String getStatus(final String toolName, final String path, final Scope scope, final String entityId) {
        return getStatusImpl(toolName, path, scope, entityId);
    }

    @Transactional
    @Override
    public void enable(String xnatUser, String reason, String toolName, String path) throws ConfigServiceException {
        setStatusImpl(xnatUser, reason, toolName, path, Configuration.ENABLED_STRING, null);
    }

    //fail silently if the configuration does not exist...
    @Transactional
    @Override
    public void enable(String xnatUser, String reason, String toolName, String path, Long projectID) throws ConfigServiceException {
        setStatusImpl(xnatUser, reason, toolName, path, Configuration.ENABLED_STRING, projectID);
    }

    @Transactional
    @Override
    public void enable(final String xnatUser, final String reason, final String toolName, final String path, final Scope scope, final String entityId) throws ConfigServiceException {
        setStatusImpl(xnatUser, reason, toolName, path, Configuration.ENABLED_STRING, scope == Scope.Site ? null : Long.parseLong(entityId));
    }

    //fail silently if the configuration does not exist...
    @Transactional
    @Override
    public void disable(String xnatUser, String reason, String toolName, String path) throws ConfigServiceException {
        setStatusImpl(xnatUser, reason, toolName, path, Configuration.DISABLED_STRING, null);
    }

    //fail silently if the configuration does not exist...
    @Transactional
    @Override
    public void disable(String xnatUser, String reason, String toolName, String path, Long projectID) throws ConfigServiceException {
        setStatusImpl(xnatUser, reason, toolName, path, Configuration.DISABLED_STRING, projectID);
    }

    @Transactional
    @Override
    public void disable(final String xnatUser, final String reason, final String toolName, final String path, final Scope scope, final String entityId) throws ConfigServiceException {
        setStatusImpl(xnatUser, reason, toolName, path, Configuration.DISABLED_STRING, scope == Scope.Site ? null : Long.parseLong(entityId));
    }

    @Transactional
    @Override
    public List<Configuration> getHistory(String toolName, String path) {
        return getHistoryImpl(toolName, path, null);
    }

    @Transactional
    @Override
    public List<Configuration> getHistory(String toolName, String path, Long projectID) {
        return getHistoryImpl(toolName, path, projectID);
    }

    @Transactional
    @Override
    public List<Configuration> getHistory(final String toolName, final String path, final Scope scope, final String entityId) {
        return getHistoryImpl(toolName, path, scope, entityId);
    }

    private List<String> getToolsImpl(Long projectID) {
        return _dao.getTools(projectID);
    }

    private List<String> getToolsImpl(Scope scope, String entityId) {
        return _dao.getTools(scope, entityId);
    }

    private List<Configuration> getConfigsByToolImpl(String toolName, Long projectID) {
        return _dao.getConfigurationsByTool(toolName, projectID);
    }

    private List<Configuration> getConfigsByToolImpl(String toolName, Scope scope, String entityId) {
        return _dao.getConfigurationsByTool(toolName, scope, entityId);
    }

    //return the most recent configuration. Null if it does not exist.
    @SuppressWarnings("unchecked")
    private Configuration getConfigImpl(String toolName, String path, Long projectID) {
        List<Configuration> list = _dao.findByToolPathProject(toolName, path, projectID);
        if (list == null || list.size() == 0) {
            return null;
        } else {
            Collections.sort(list, ConfigComparatorByCreateDate);
            return list.get(list.size() - 1);
        }
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

    //return the most recent configuration contents. Null if it does not exist.
    @SuppressWarnings("unchecked")
    private String getConfigContentsImpl(String toolName, String path, Long projectID) {
        List<Configuration> list = _dao.findByToolPathProject(toolName, path, projectID);
        if (list == null || list.size() == 0) {
            return null;
        } else {
            Collections.sort(list, ConfigComparatorByCreateDate);
            return list.get(list.size() - 1).getContents();
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

    private Configuration getConfigByIdImpl(String toolName, String path, String id, Long projectID, Scope scope, String entityId) {
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
        if (StringUtils.equals(c.getTool(), toolName) && StringUtils.equals(c.getPath(), path) && (ObjectUtils.nullSafeEquals((c.getProject()), projectID) || (c.getScope() == scope && StringUtils.equals(entityId, c.getEntityId())))) {
            return c;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Configuration getConfigByVersionImpl(String toolName, String path, int version, Long projectID) {
        List<Configuration> list = _dao.findByToolPathProject(toolName, path, projectID);
        if (list == null || list.size() < version) {
            return null;
        } else {
            Collections.sort(list, ConfigComparatorByVersion);
            Configuration ret = list.get(version - 1);
            //this will only fail if something truly stupid happened. Still should check, though.
            if (ret.getVersion() == version) {
                return ret;
            } else {
                //something odd happened, let's search the list for the version
                for (Configuration c : list) {
                    if (c.getVersion() == version) {
                        return c;
                    }
                }
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Configuration getConfigByVersionImpl(String toolName, String path, int version, Scope scope, String entityId) {
        List<Configuration> list = _dao.findByToolPathProject(toolName, path, scope, entityId);
        if (list == null || list.size() < version) {
            return null;
        } else {
            Collections.sort(list, ConfigComparatorByVersion);
            Configuration ret = list.get(version - 1);
            //this will only fail if something truly stupid happened. Still should check, though.
            if (ret.getVersion() == version) {
                return ret;
            } else {
                //something odd happened, let's search the list for the version
                for (Configuration c : list) {
                    if (c.getVersion() == version) {
                        return c;
                    }
                }
                return null;
            }
        }
    }

    private Configuration replaceConfigImpl(String xnatUser, String reason, String toolName, String path, String status, Boolean unversioned, String contents, Long projectID, final Scope scope, final String entityId) throws ConfigServiceException {

        if (contents != null && contents.length() > ConfigService.MAX_FILE_LENGTH) {
            throw new ConfigServiceException("file size must be less than " + ConfigService.MAX_FILE_LENGTH + " characters.");
        }

        //if a current config exists and the contents are the same as the previous version, share the config data
        final boolean usesProjectId = scope == null && StringUtils.isBlank(entityId);
        Configuration oldConfig = usesProjectId ? getConfigImpl(toolName, path, projectID) : getConfigImpl(toolName, path, scope, entityId);

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
        if (usesProjectId) {
            configuration.setProject(projectID);
        } else {
            configuration.setScope(scope);
            configuration.setEntityId(entityId);
        }
        configuration.setXnatUser(xnatUser);
        configuration.setReason(reason);
        configuration.setStatus(StringUtils.isBlank(status) ? Configuration.ENABLED_STRING : status);
        configuration.setVersion(1 + ((oldConfig != null && doVersion) ? oldConfig.getVersion() : 0));
        configuration.setUnversioned(!doVersion);

        if (update) {
            _dao.update(configuration);
        } else {
            _dao.create(configuration);
        }
        return configuration;
    }

    @SuppressWarnings("unchecked")
    private String getStatusImpl(String toolName, String path, Long projectID) {
        List<Configuration> list = _dao.findByToolPathProject(toolName, path, projectID);
        if (list == null || list.size() == 0) return null;
        Collections.sort(list, ConfigComparatorByCreateDate);
        return list.get(list.size() - 1).getStatus();
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
    private void setStatusImpl(String xnatUser, String reason, String toolName, String path, String status, Long projectID) throws ConfigServiceException {

        List<Configuration> list = _dao.findByToolPathProject(toolName, path, projectID);
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
            replaceConfigImpl(xnatUser, reason, toolName, path, status, entity.isUnversioned(), entity.getContents(), entity.getProject(), null, null);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Configuration> getHistoryImpl(String toolName, String path, Long projectID) {

        List<Configuration> list = _dao.findByToolPathProject(toolName, path, projectID);
        if (list == null || list.size() == 0) return null;
        Collections.sort(list, ConfigComparatorByCreateDate);
        return list;
    }

    @SuppressWarnings("unchecked")
    private List<Configuration> getHistoryImpl(String toolName, String path, Scope scope, String entityId) {

        List<Configuration> list = _dao.findByToolPathProject(toolName, path, scope, entityId);
        if (list == null || list.size() == 0) return null;
        Collections.sort(list, ConfigComparatorByCreateDate);
        return list;
    }

    @Inject
    private ConfigurationDAO _dao;

    @Inject
    private ConfigurationDataDAO _dataDAO;
}
