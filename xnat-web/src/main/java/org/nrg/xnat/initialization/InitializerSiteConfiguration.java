/**
 * InitializerSiteConfig
 * (C) 2016 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 */
package org.nrg.xnat.initialization;

import org.apache.commons.lang3.StringUtils;
import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.config.services.impl.PropertiesBasedSiteConfigurationService;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.services.SerializerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

@Service
public class InitializerSiteConfiguration extends PropertiesBasedSiteConfigurationService {
    public String getSiteConfigurationService() throws SiteConfigurationException {
        return getSiteConfigurationProperty("admin.siteConfig.service");
    }

    public String getPasswordComplexity() throws SiteConfigurationException {
        return getSiteConfigurationProperty("passwordComplexity");
    }

    public String getPasswordComplexityMessage() throws SiteConfigurationException {
        return getSiteConfigurationProperty("passwordComplexityMessage");
    }

    public int getPasswordHistoryDuration() throws SiteConfigurationException {
        return getIntegerSiteConfigurationProperty("passwordHistoryDuration");
    }

    public String getReceivedFileUser() throws SiteConfigurationException {
        return getSiteConfigurationProperty("receivedFileUser");
    }

    public int getInactivityBeforeLockout() throws SiteConfigurationException {
        return getIntegerSiteConfigurationProperty("inactivityBeforeLockout");
    }

    public long getInactivityBeforeLockoutSchedule() throws SiteConfigurationException {
        return getLongSiteConfigurationProperty("inactivityBeforeLockoutSchedule");
    }

    public long getMaxFailedLoginsLockoutDuration() throws SiteConfigurationException {
        return getLongSiteConfigurationProperty("maxFailedLoginsLockoutDuration");
    }

    public double getSessionXmlRebuilderInterval() throws SiteConfigurationException {
        return getDoubleSiteConfigurationProperty("sessionXmlRebuilderInterval");
    }

    public long getSessionXmlRebuilderRepeat() throws SiteConfigurationException {
        return getLongSiteConfigurationProperty("sessionXmlRebuilderRepeat");
    }

    public String getAliasTokenTimeout() throws SiteConfigurationException {
        return getSiteConfigurationProperty("aliasTokenTimeout");
    }

    public Map<String, String> getSmtpServer() throws SiteConfigurationException, IOException {
        final String definition = getSiteConfigurationProperty("smtpServer");
        if (StringUtils.isBlank(definition)) {
            return null;
        }
        return _serializerService.deserializeJsonToMapOfStrings(definition);
    }

    public String getAdminEmail() throws SiteConfigurationException {
        return getSiteConfigurationProperty("adminEmail");
    }

    public String getEmailPrefix() throws SiteConfigurationException {
        return getSiteConfigurationProperty("emailPrefix");
    }

    public String getFeatureService() throws SiteConfigurationException {
        return getSiteConfigurationProperty("featureService");
    }

    public String getFeatureRepositoryService() throws SiteConfigurationException {
        return getSiteConfigurationProperty("featureRepositoryService");
    }

    public String getRoleService() throws SiteConfigurationException {
        return getSiteConfigurationProperty("roleService");
    }

    public String getRoleRepositoryService() throws SiteConfigurationException {
        return getSiteConfigurationProperty("roleRepositoryService");
    }

    @Override
    protected void setPreferenceValue(final String username, final String property, final String value) {
        throw new NrgServiceRuntimeException(NrgServiceError.PermissionsViolation, "This site configuration service is for initialization and is read only.");
    }

    @Override
    protected void getPreferenceValuesFromPersistentStore(final Properties properties) {
        final Integer siteConfigToolKey = getSiteConfigToolKey();
        if (siteConfigToolKey == null) {
            _log.info("Didn't find a tool for the {} ID, checking for import values from configuration service.", SITE_CONFIG_TOOL_ID);
            final Properties existing = checkForConfigServiceSiteConfiguration();
            if (existing != null) {
                _log.info("Found {} properties in the configuration service, importing those.", existing.size());
                properties.putAll(existing);
            }
        } else {
            _log.info("Working with the existing {} tool, checking for new import values.", SITE_CONFIG_TOOL_ID);
            properties.putAll(getPersistedSiteConfiguration(siteConfigToolKey));
        }
    }

    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    private Properties getPersistedSiteConfiguration(final int toolId) {
        return getJdbcTemplate().query("select name, value from xhbm_preference where tool = ?", PROPERTIES_RESULT_SET_EXTRACTOR, toolId);
    }

    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    private Integer getSiteConfigToolKey() {
        return getJdbcTemplate().queryForObject("select id from xhbm_tool where tool_id = ?", Integer.class, SITE_CONFIG_TOOL_ID);
    }

    private static final ResultSetExtractor<Properties> PROPERTIES_RESULT_SET_EXTRACTOR = new ResultSetExtractor<Properties>() {
        @Override
        public Properties extractData(final ResultSet results) throws SQLException, DataAccessException {
            final Properties properties = new Properties();
            while (results.next()) {
                properties.put(results.getString("name"), results.getString("value"));
            }
            return properties;
        }
    };

    private static final Logger _log                = LoggerFactory.getLogger(InitializerSiteConfiguration.class);
    private static final String SITE_CONFIG_TOOL_ID = "siteConfig";

    @Inject
    private SerializerService _serializerService;
}
