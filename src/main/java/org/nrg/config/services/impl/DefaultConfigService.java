/*
 * org.nrg.config.services.impl.DefaultConfigService
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 5:30 PM
 */
package org.nrg.config.services.impl;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang.StringUtils;
import org.nrg.config.daos.ConfigurationDAO;
import org.nrg.config.daos.ConfigurationDataDAO;
import org.nrg.config.entities.Configuration;
import org.nrg.config.entities.ConfigurationData;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.List;

@Service
public class DefaultConfigService implements ConfigService {

    public static BeanComparator ConfigComparatorByCreateDate = new BeanComparator("created");
    public static BeanComparator ConfigComparatorByVersion = new BeanComparator("version");

    @Autowired
    private ConfigurationDAO configurationDAO;

    @Autowired
    private ConfigurationDataDAO configurationDataDAO;

    @Transactional
    @Override
    public List<Configuration> getAll() {
        return configurationDAO.getAll();
    }

    @Transactional
    @Override
    public Configuration getById(Long id) {
        return configurationDAO.findById(id);
    }

    @Transactional
    @Override
    public List<Long> getProjects() {
        return getProjects(null);
    }

    @Transactional
    @Override
    public List<Long> getProjects(String toolName) {
        return configurationDAO.getProjects(toolName);
    }

    @Transactional
    private List<String> getToolsImpl(Long projectID) {
        return configurationDAO.getTools(projectID);
    }

    @Transactional
    private List<Configuration> getConfigsByToolImpl(String toolName, Long projectID) {
        return configurationDAO.getConfigurationsByTool(toolName, projectID);
    }

    //return the most recent configuration. Null if it does not exist.
    @SuppressWarnings("unchecked")
    @Transactional
    private Configuration getConfigImpl(String toolName, String path, Long projectID) {
        List<Configuration> list = configurationDAO.findByToolPathProject(toolName, path, projectID);
        if (list == null || list.size() == 0) {
            return null;
        } else {
            Collections.sort(list, ConfigComparatorByCreateDate);
            return list.get(list.size() - 1);
        }
    }

    //return the most recent configuration contents. Null if it does not exist.
    @SuppressWarnings("unchecked")
    @Transactional
    private String getConfigContentsImpl(String toolName, String path, Long projectID) {
        List<Configuration> list = configurationDAO.findByToolPathProject(toolName, path, projectID);
        if (list == null || list.size() == 0) {
            return null;
        } else {
            Collections.sort(list, ConfigComparatorByCreateDate);
            return list.get(list.size() - 1).getContents();
        }
    }

    @Transactional
    private Configuration getConfigByIdImpl(String toolName, String path, String id, Long projectID) {
        //I think it is more efficient to just pull by the ID and make sure it matches the other passed in variables.
        Configuration c = configurationDAO.findById(Long.parseLong(id));

        //findById is silly in that it will return a non-null object even if it doesn't find a match (I didn't write it). If it didn't find a match it
        //will throw an exception the first time you try to access a property. So, we'll use that to test for a valid return, here.
        //this also takes care of a null return.
        try {
            c.getTool();
        } catch (Exception e) {
            return null;
        }
        if (StringUtils.equals(c.getTool(), toolName) && StringUtils.equals(c.getPath(), path) && ObjectUtils.nullSafeEquals((c.getProject()), projectID)) {
            return c;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Transactional
    private Configuration getConfigByVersionImpl(String toolName, String path, int version, Long projectID) {
        List<Configuration> list = configurationDAO.findByToolPathProject(toolName, path, projectID);
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

    @Transactional
    private Configuration replaceConfigImpl(String xnatUser, String reason, String toolName, String path, Boolean unversioned, String contents, Long projectID) throws ConfigServiceException {

        if (contents != null && contents.length() > ConfigService.MAX_FILE_LENGTH) {
            throw new ConfigServiceException("file size must be less than " + ConfigService.MAX_FILE_LENGTH + " characters.");
        }

        //if a current config exists and the contents are the same as the previous version, share the config data
        Configuration oldConfig = getConfig(toolName, path, projectID);

        // We will do versioning if:
        //  Case1) There is no config and unversioned is not specified (defaults to versioning) or unversioned is false
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
                configurationDataDAO.delete(configuration.getConfigData());
                configuration.setConfigData(null);
            }
        } else {
            // But if not those things, we're creating a new (possibly versioned or non-versioned, we don't care) configuration
            update = false;
            configuration = new Configuration();
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
                configurationDataDAO.create(configurationData);
                configuration.setConfigData(configurationData);
            }
        }

        configuration.setTool(toolName);
        configuration.setPath(path);
        configuration.setProject(projectID);
        configuration.setXnatUser(xnatUser);
        configuration.setReason(reason);
        configuration.setStatus(Configuration.ENABLED_STRING);
        configuration.setVersion(1 + ((oldConfig != null && doVersion) ? oldConfig.getVersion() : 0));
        configuration.setUnversioned(!doVersion);

        if (update) {
            configurationDAO.update(configuration);
        } else {
            configurationDAO.create(configuration);
        }
        return configuration;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    private String getStatusImpl(String toolName, String path, Long projectID) {
        List<Configuration> list = configurationDAO.findByToolPathProject(toolName, path, projectID);
        if (list == null || list.size() == 0) return null;
        Collections.sort(list, ConfigComparatorByCreateDate);
        return list.get(list.size() - 1).getStatus();
    }


    //fail silently if the configuration does not exist, throws an unsupported operation exception if status is null.
    @SuppressWarnings("unchecked")
    @Transactional
    private void setStatus(String xnatUser, String reason, String toolName, String path, String status, Long projectID) {

        if (status == null) {
            throw new UnsupportedOperationException("Unable to set a configuration's status to null");
        }

        List<Configuration> list = configurationDAO.findByToolPathProject(toolName, path, projectID);
        if (list == null || list.size() == 0) {
            //fail silently if the configuration does not exist...
            return;
        }
        Collections.sort(list, ConfigComparatorByCreateDate);
        Configuration entity = list.get(list.size() - 1);

        // Only change the status if the status is actually changing.
        if (!status.equals(entity.getStatus())) {
            if (!entity.isUnversioned()) {
                //changing an existing versioned Configuration is a no-no. We have to create a new Configuration with a new xnatUser and reason
                Configuration newConfig = new Configuration();
                newConfig.setProject(entity.getProject());
                newConfig.setTool(entity.getTool());
                newConfig.setPath(entity.getPath());
                newConfig.setConfigData(entity.getConfigData());
                newConfig.setStatus(status);
                newConfig.setXnatUser(xnatUser);
                newConfig.setReason(reason);
                configurationDAO.create(newConfig);
            } else {
                entity.setStatus(status);
                entity.setStatus(xnatUser);
                entity.setStatus(reason);
                configurationDAO.update(entity);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Transactional
    private List<Configuration> getHistoryImpl(String toolName, String path, Long projectID) {

        List<Configuration> list = configurationDAO.findByToolPathProject(toolName, path, projectID);
        if (list == null || list.size() == 0) return null;
        Collections.sort(list, ConfigComparatorByCreateDate);
        return list;
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
    public Configuration getConfigById(String toolName, String path, String id) {
        return getConfigByIdImpl(toolName, path, id, null);
    }

    @Transactional
    @Override
    public Configuration getConfigById(String toolName, String path, String id, Long projectID) {
        return getConfigByIdImpl(toolName, path, id, projectID);
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
    public Configuration replaceConfig(final String xnatUser, final String reason, final String toolName, final String path, final Boolean unversioned, final String contents) throws ConfigServiceException {
        return replaceConfigImpl(xnatUser, reason, toolName, path, unversioned, contents, null);
    }

    @Transactional
    @Override
    public Configuration replaceConfig(final String xnatUser, final String reason, final String toolName, final String path, final Boolean unversioned, final String contents, final Long projectID) throws ConfigServiceException {
        return replaceConfigImpl(xnatUser, reason, toolName, path, unversioned, contents, projectID);
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
    public void enable(String xnatUser, String reason, String toolName, String path) {
        setStatus(xnatUser, reason, toolName, path, Configuration.ENABLED_STRING, null);
    }

    //fail silently if the configuration does not exist...
    @Transactional
    @Override
    public void enable(String xnatUser, String reason, String toolName, String path, Long projectID) {
        setStatus(xnatUser, reason, toolName, path, Configuration.ENABLED_STRING, projectID);
    }

    //fail silently if the configuration does not exist...
    @Transactional
    @Override
    public void disable(String xnatUser, String reason, String toolName, String path) {
        setStatus(xnatUser, reason, toolName, path, Configuration.DISABLED_STRING, null);
    }

    //fail silently if the configuration does not exist...
    @Transactional
    @Override
    public void disable(String xnatUser, String reason, String toolName, String path, Long projectID) {
        setStatus(xnatUser, reason, toolName, path, Configuration.DISABLED_STRING, projectID);
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
}
