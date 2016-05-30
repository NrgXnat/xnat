/**
 * InitializerSiteConfig
 * (C) 2016 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 */
package org.nrg.xdat.preferences;

import org.apache.commons.lang3.StringUtils;
import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.config.services.impl.PropertiesBasedSiteConfigurationService;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.hibernate.HibernateUtils;
import org.nrg.framework.services.SerializerService;
import org.nrg.prefs.services.PreferenceBeanHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

@Service
public class InitializerSiteConfiguration extends PropertiesBasedSiteConfigurationService {
    public String getSiteConfigurationService() throws SiteConfigurationException {
        return getSiteConfigurationProperty("admin.siteConfig.service");
    }

    public boolean getRequireLogin() {
        return getBoolSiteConfigurationProperty("requireLogin", true);
    }

    public boolean getEmailVerification() {
        return getBoolSiteConfigurationProperty("emailVerification", true);
    }

    public boolean getRequireSaltedPasswords() {
        return getBoolSiteConfigurationProperty("requireSaltedPasswords", true);
    }

    public String getPasswordExpirationType() throws SiteConfigurationException {
        return getSiteConfigurationProperty("passwordExpirationType");
    }

    public int getPasswordExpirationInterval() throws SiteConfigurationException {
        return getIntegerSiteConfigurationProperty("passwordExpirationInterval");
    }

    public Date getPasswordExpirationDate() throws SiteConfigurationException {
        final String dateValue = getSiteConfigurationProperty("passwordExpirationDate");
        if (StringUtils.isBlank(dateValue)) {
            return null;
        }
        try {
            return new Date(Long.parseLong(dateValue));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getPasswordComplexity() throws SiteConfigurationException {
        return getSiteConfigurationProperty("passwordComplexity");
    }

    public String getPasswordComplexityMessage() throws SiteConfigurationException {
        return getSiteConfigurationProperty("passwordComplexityMessage");
    }

    public String getPasswordHistoryDuration() throws SiteConfigurationException {
        return getSiteConfigurationProperty("passwordHistoryDuration");
    }

    public String getReceivedFileUser() throws SiteConfigurationException {
        return getSiteConfigurationProperty("receivedFileUser");
    }

    public int getInactivityBeforeLockout() throws SiteConfigurationException {
        return getIntegerSiteConfigurationProperty("inactivityBeforeLockout");
    }

    public String getInactivityBeforeLockoutSchedule() throws SiteConfigurationException {
        return getSiteConfigurationProperty("inactivityBeforeLockoutSchedule");
    }

    public int getMaxFailedLogins() throws SiteConfigurationException {
        return getIntegerSiteConfigurationProperty("maxFailedLogins");
    }

    public String getMaxFailedLoginsLockoutDuration() throws SiteConfigurationException {
        return getSiteConfigurationProperty("maxFailedLoginsLockoutDuration");
    }

    public double getSessionXmlRebuilderInterval() throws SiteConfigurationException {
        return getDoubleSiteConfigurationProperty("sessionXmlRebuilderInterval");
    }

    public long getSessionXmlRebuilderRepeat() throws SiteConfigurationException {
        return getLongSiteConfigurationProperty("sessionXmlRebuilderRepeat");
    }

    public long getAliasTokenTimeout() throws SiteConfigurationException {
        return getLongSiteConfigurationProperty("aliasTokenTimeout");
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

    public String getSecurityChannel() throws SiteConfigurationException {
        return getSiteConfigurationProperty("security.channel");
    }

    public int getConcurrentMaxSessions() throws SiteConfigurationException {
        return getIntegerSiteConfigurationProperty("sessions.concurrent_max");
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
    protected void setPreferenceValue(final String username, final String property, final String value) throws SiteConfigurationException {
        final int toolId = getSiteConfigToolKey();
        final String current = getSiteConfigurationProperty(property);
        final boolean exists = !StringUtils.isBlank(current) || getJdbcTemplate().queryForObject("select count(*) as item_count from xhbm_preference where tool = ? and name = ?", Integer.class, toolId, property) != 0;
        final Timestamp timestamp = new Timestamp(new Date().getTime());
        if (exists && !StringUtils.equals(current, value)) {
            getJdbcTemplate().update("update xhbm_preference set value = ?, set \"timestamp\" = ? where tool = ? and name = ?", value, timestamp, toolId, property);
        } else if (!exists) {
            getJdbcTemplate().update("insert into xhbm_preference (created, disabled, enabled, \"timestamp\", entity_id, name, scope, value, tool) values (?, ?, true, ?, null, ?, 0, ?, ?)", timestamp, new Timestamp(0), timestamp, property, value, toolId);
        }
        setSiteConfigurationProperty(username, property, value);
    }

    @Override
    protected void getPreferenceValuesFromPersistentStore(final Properties properties) {
        properties.putAll(PreferenceBeanHelper.getPreferenceBeanProperties(SiteConfigPreferences.class));
        try {
            final Map<String, Boolean> tablesExist = HibernateUtils.checkTablesExist(getJdbcTemplate().getDataSource(), "xhbm_configuration", "xhbm_tool", "xhbm_preference");
            //noinspection RedundantArrayCreation
            if (tablesExist.get("xhbm_tool") && tablesExist.get("xhbm_preference")) {
                _log.info("Working with the existing {} tool, checking for new import values.", SITE_CONFIG_TOOL_ID);
                final Integer siteConfigToolKey = getSiteConfigToolKey();
                properties.putAll(getPersistedSiteConfiguration(siteConfigToolKey));
            } else if (tablesExist.get("xhbm_configuration")) {
                _log.info("The tool and preference tables don't exist, checking for import values from configuration service.");
                final Properties existing = checkForConfigServiceSiteConfiguration();
                if (existing != null) {
                    _log.info("Found {} properties in the configuration service, importing those.", existing.size());
                    properties.putAll(existing);
                }
            } else {
                _log.warn("The tool, preference, and configuration tables don't exist, using default properties imported from configuration properties files and site configuration preferences beans.");
            }
        } catch (SQLException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred trying to retrieve preferences from the database.", e);
        }
    }

    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    private Integer getSiteConfigToolKey() {
        return getJdbcTemplate().queryForObject("select id from xhbm_tool where tool_id = ?", Integer.class, SITE_CONFIG_TOOL_ID);
    }

    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    private Properties getPersistedSiteConfiguration(final int toolId) {
        return getJdbcTemplate().query("select name, value from xhbm_preference where tool = ?", PROPERTIES_RESULT_SET_EXTRACTOR, toolId);
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
