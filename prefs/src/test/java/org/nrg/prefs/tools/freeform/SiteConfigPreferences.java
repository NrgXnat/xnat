package org.nrg.prefs.tools.freeform;
/*
 * core: org.nrg.xdat.preferences.SiteConfigPreferences
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

import lombok.extern.slf4j.Slf4j;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * This is a subset of preferences from the version of this class defined in XDAT.
 */
@SuppressWarnings("unused")
@NrgPreferenceBean(toolId = SiteConfigPreferences.SITE_CONFIG_TOOL_ID,
                   toolName = "XNAT Site Preferences",
                   description = "Manages site configurations and settings for the XNAT system.",
                   strict = false)
@Slf4j
public class SiteConfigPreferences extends AbstractPreferenceBean {
    public static final String SITE_CONFIG_TOOL_ID = "siteConfig";
    public static final String INITIALIZED         = "initialized";
    public static final String SITE_URL            = "siteUrl";

    @Autowired
    public SiteConfigPreferences(final NrgPreferenceService preferenceService) {
        super(preferenceService, null, null);
    }

    @NrgPreference(defaultValue = "you@yoursite.org")
    public String getAdminEmail() {return getValue("adminEmail");}

    public void setAdminEmail(final String adminEmail) {
        try {
            set(adminEmail, "adminEmail");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'adminEmail': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "2 days")
    public String getAliasTokenTimeout() {return getValue("aliasTokenTimeout");}

    public void setAliasTokenTimeout(final String aliasTokenTimeout) {
        try {
            set(aliasTokenTimeout, "aliasTokenTimeout");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'aliasTokenTimeout': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "0 0 * * * *")
    // 0 0 * * * * means it runs every hour
    public String getAliasTokenTimeoutSchedule() {return getValue("aliasTokenTimeoutSchedule");}

    public void setAliasTokenTimeoutSchedule(final String aliasTokenTimeoutSchedule) {
        try {
            set(aliasTokenTimeoutSchedule, "aliasTokenTimeoutSchedule");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'aliasTokenTimeoutSchedule': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", aliases = "security.allow-data-admins")
    public boolean getAllowDataAdmins() {return getBooleanValue("allowDataAdmins");}

    public void setAllowDataAdmins(final boolean allowDataAdmins) {
        try {
            setBooleanValue(allowDataAdmins, "allowDataAdmins");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'allowDataAdmins': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", aliases = "security.allow-HTML-resource-rendering")
    public boolean getAllowHtmlResourceRendering() {return getBooleanValue("allowHtmlResourceRendering");}

    public void setAllowHtmlResourceRendering(final String allowHtmlResourceRendering) {
        try {
            set(allowHtmlResourceRendering, "allowHtmlResourceRendering");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'allowHtmlResourceRendering': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean getAllowNonAdminsToClaimUnassignedSessions() {return getBooleanValue("allowNonAdminsToClaimUnassignedSessions");}

    public void setAllowNonAdminsToClaimUnassignedSessions(final boolean allowNonAdminsToClaimUnassignedSessions) {
        try {
            setBooleanValue(allowNonAdminsToClaimUnassignedSessions, "allowNonAdminsToClaimUnassignedSessions");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'allowNonAdminsToClaimUnassignedSessions': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/data/xnat/archive")
    public String getArchivePath() {return getValue("archivePath");}

    public void setArchivePath(final String archivePath) {
        try {
            set(archivePath, "archivePath");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'archivePath': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/data/xnat/build")
    public String getBuildPath() {return getValue("buildPath");}

    public void setBuildPath(final String buildPath) {
        try {
            set(buildPath, "buildPath");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'buildPath': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/data/xnat/cache")
    public String getCachePath() {return getValue("cachePath");}

    public void setCachePath(final String cachePath) {
        try {
            set(cachePath, "cachePath");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'cachePath': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean getChecksums() {return getBooleanValue("checksums");}

    public void setChecksums(final boolean checksums) {
        try {
            setBooleanValue(checksums, "checksums");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'checksums': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "['/xapi/**', '/data/**', '/REST/**', '/fs/**']")
    public List<String> getDataPaths() {return getListValue("dataPaths");}

    public void setDataPaths(final List<String> dataPaths) {
        try {
            setListValue("dataPaths", dataPaths);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'dataPaths': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean getEnableCsrfToken() {return getBooleanValue("enableCsrfToken");}

    public void setEnableCsrfToken(final boolean enableCsrfToken) {
        try {
            setBooleanValue(enableCsrfToken, "enableCsrfToken");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'enableCsrfToken': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean isEnableDicomReceiver() {return getBooleanValue("enableDicomReceiver");}

    public void setEnableDicomReceiver(final boolean enableDicomReceiver) {
        try {
            setBooleanValue(enableDicomReceiver, "enableDicomReceiver");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'enableDicomReceiver': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "org.nrg.dcm.DicomSCPSiteConfigurationListener", aliases = "enableDicomReceiver.property.changed.listener")
    public String getEnableDicomReceiverPropertyChangedListener() {return getValue("enableDicomReceiverPropertyChangedListener");}

    public void setEnableDicomReceiverPropertyChangedListener(final String enableDicomReceiverPropertyChangedListener) {
        try {
            set(enableDicomReceiverPropertyChangedListener, "enableDicomReceiverPropertyChangedListener");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'enableDicomReceiverPropertyChangedListener': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean getEnableSitewideAnonymizationScript() {return getBooleanValue("enableSitewideAnonymizationScript");}

    public void setEnableSitewideAnonymizationScript(final boolean enableSitewideAnonymizationScript) {
        try {
            setBooleanValue(enableSitewideAnonymizationScript, "enableSitewideAnonymizationScript");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'enableSitewideAnonymizationScript': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false")
    public boolean getEnableSitewideSeriesImportFilter() {return getBooleanValue("enableSitewideSeriesImportFilter");}

    public void setEnableSitewideSeriesImportFilter(final boolean enableSitewideSeriesImportFilter) {
        try {
            setBooleanValue(enableSitewideSeriesImportFilter, "enableSitewideSeriesImportFilter");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'enableSitewideSeriesImportFilter': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "['localdb']")
    public List<String> getEnabledProviders() {return getListValue("enabledProviders");}

    public void setEnabledProviders(final List<String> enabledProviders) {
        try {
            setListValue("enabledProviders", enabledProviders);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name enabledProviders: something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/data/xnat/ftp")
    public String getFtpPath() {return getValue("ftpPath");}

    public void setFtpPath(final String ftpPath) {
        try {
            set(ftpPath, "ftpPath");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'ftpPath': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "1 year")
    public String getInactivityBeforeLockout() {return getValue("inactivityBeforeLockout");}

    public void setInactivityBeforeLockout(final String inactivityBeforeLockout) {
        try {
            set(inactivityBeforeLockout, "inactivityBeforeLockout");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'inactivityBeforeLockout': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "0 0 1 * * ?")
    // 0 0 1 * * ? means it runs at 1AM every day
    public String getInactivityBeforeLockoutSchedule() {return getValue("inactivityBeforeLockoutSchedule");}

    public void setInactivityBeforeLockoutSchedule(final String inactivityBeforeLockoutSchedule) {
        try {
            set(inactivityBeforeLockoutSchedule, "inactivityBeforeLockoutSchedule");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'inactivityBeforeLockoutSchedule': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/data/xnat/inbox")
    public String getInboxPath() {return getValue("inboxPath");}

    public void setInboxPath(final String inboxPath) {
        try {
            set(inboxPath, "inboxPath");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'inboxPath': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false")
    public boolean isInitialized() {return getBooleanValue(INITIALIZED);}

    public void setInitialized(final boolean initialized) {
        try {
            setBooleanValue(initialized, INITIALIZED);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name {}: something is very wrong here.", INITIALIZED, e);
        }
    }

    @NrgPreference(defaultValue = "['.*MSIE.*', '.*Mozilla.*', '.*AppleWebKit.*', '.*Opera.*']")
    public List<String> getInteractiveAgentIds() {return getListValue("interactiveAgentIds");}

    public void setInteractiveAgentIds(final List<String> interactiveAgentIds) {
        try {
            setListValue("interactiveAgentIds", interactiveAgentIds);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'interactiveAgentIds': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "20")
    public int getMaxFailedLogins() {return getIntegerValue("maxFailedLogins");}

    public void setMaxFailedLogins(final int maxFailedLogins) {
        try {
            setIntegerValue(maxFailedLogins, "maxFailedLogins");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'maxFailedLogins': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "1 hour")
    public String getMaxFailedLoginsLockoutDuration() {return getValue("maxFailedLoginsLockoutDuration");}

    public void setMaxFailedLoginsLockoutDuration(final String maxFailedLoginsLockoutDuration) {
        try {
            set(maxFailedLoginsLockoutDuration, "maxFailedLoginsLockoutDuration");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'maxFailedLoginsLockoutDuration': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "^.*$")
    public String getPasswordComplexity() {return getValue("passwordComplexity");}

    public void setPasswordComplexity(final String passwordComplexity) {
        try {
            set(passwordComplexity, "passwordComplexity");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'passwordComplexity': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "Password is not sufficiently complex.")
    public String getPasswordComplexityMessage() {return getValue("passwordComplexityMessage");}

    public void setPasswordComplexityMessage(final String passwordComplexityMessage) {
        try {
            set(passwordComplexityMessage, "passwordComplexityMessage");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'passwordComplexityMessage': something is very wrong here.", e);
        }
    }

    @NrgPreference
    public String getPasswordExpirationDate() {return getValue("passwordExpirationDate");}

    public void setPasswordExpirationDate(final String passwordExpirationDate) {
        try {
            set(passwordExpirationDate, "passwordExpirationDate");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'passwordExpirationDate': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "1 year")
    public String getPasswordExpirationInterval() {return getValue("passwordExpirationInterval");}

    public void setPasswordExpirationInterval(final String passwordExpirationInterval) {
        try {
            set(passwordExpirationInterval, "passwordExpirationInterval");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'passwordExpirationInterval': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "Interval")
    public String getPasswordExpirationType() {return getValue("passwordExpirationType");}

    public void setPasswordExpirationType(final String passwordExpirationType) {
        try {
            set(passwordExpirationType, "passwordExpirationType");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'passwordExpirationType': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "1 year")
    public String getPasswordHistoryDuration() {return getValue("passwordHistoryDuration");}

    public void setPasswordHistoryDuration(final String passwordHistoryDuration) {
        try {
            set(passwordHistoryDuration, "passwordHistoryDuration");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'passwordHistoryDuration': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "None")
    public String getPasswordReuseRestriction() {return getValue("passwordReuseRestriction");}

    public void setPasswordReuseRestriction(final String passwordReuseRestriction) {
        try {
            set(passwordReuseRestriction, "passwordReuseRestriction");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'passwordReuseRestriction': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/data/xnat/pipeline")
    public String getPipelinePath() {return getValue("pipelinePath");}

    public void setPipelinePath(final String pipelinePath) {
        try {
            set(pipelinePath, "pipelinePath");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'pipelinePath': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/data/xnat/prearchive")
    public String getPrearchivePath() {return getValue("prearchivePath");}

    public void setPrearchivePath(final String prearchivePath) {
        try {
            set(prearchivePath, "prearchivePath");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'prearchivePath': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "admin")
    public String getPrimaryAdminUsername() {return getValue("primaryAdminUsername");}

    public void setPrimaryAdminUsername(final String primaryAdminUsername) {
        try {
            set(primaryAdminUsername, "primaryAdminUsername");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'primaryAdminUsername': something is very wrong here.", e);
        }
    }

    @NrgPreference
    public String getProcessingUrl() {return getValue("processingUrl");}

    public void setProcessingUrl(final String processingUrl) {
        try {
            set(processingUrl, "processingUrl");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name processingUrl: something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", aliases = "audit.require_change_justification")
    public boolean getRequireChangeJustification() {return getBooleanValue("requireChangeJustification");}

    public void setRequireChangeJustification(final boolean requireChangeJustification) {
        try {
            setBooleanValue(requireChangeJustification, "requireChangeJustification");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'requireChangeJustification': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", aliases = "audit.require_event_name")
    public boolean getRequireEventName() {return getBooleanValue("requireEventName");}

    public void setRequireEventName(final boolean requireEventName) {
        try {
            setBooleanValue(requireEventName, "requireEventName");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'requireEventName': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", aliases = "security.require_image_assessor_labels")
    public boolean getRequireImageAssessorLabels() {return getBooleanValue("requireImageAssessorLabels");}

    public void setRequireImageAssessorLabels(final boolean requireImageAssessorLabels) {
        try {
            setBooleanValue(requireImageAssessorLabels, "requireImageAssessorLabels");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'requireImageAssessorLabels': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean getRequireLogin() {return getBooleanValue("requireLogin");}

    public void setRequireLogin(final boolean requireLogin) {
        try {
            setBooleanValue(requireLogin, "requireLogin");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'requireLogin': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false")
    public boolean getRequireSaltedPasswords() {return getBooleanValue("requireSaltedPasswords");}

    public void setRequireSaltedPasswords(final boolean requireSaltedPasswords) {
        try {
            setBooleanValue(requireSaltedPasswords, "requireSaltedPasswords");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'requireSaltedPasswords': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "15 minutes")
    public String getSessionTimeout() {return getValue("sessionTimeout");}

    public void setSessionTimeout(final String sessionTimeout) {
        try {
            set(sessionTimeout, "sessionTimeout");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'sessionTimeout': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "Session timed out at TIMEOUT_TIME.")
    public String getSessionTimeoutMessage() {return getValue("sessionTimeoutMessage");}

    public void setSessionTimeoutMessage(final String sessionTimeoutMessage) {
        try {
            set(sessionTimeoutMessage, "sessionTimeoutMessage");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'sessionTimeoutMessage': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "XNAT")
    public String getSiteId() {return getValue("siteId");}

    public void setSiteId(final String siteId) {
        try {
            set(siteId, "siteId");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name siteId: something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/images/logo.png")
    public String getSiteLogoPath() {return getValue("siteLogoPath");}

    public void setSiteLogoPath(final String siteLogoPath) {
        try {
            set(siteLogoPath, "siteLogoPath");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'siteLogoPath': something is very wrong here.", e);
        }
    }

    @NrgPreference
    public String getSiteUrl() {return getValue(SITE_URL);}

    public void setSiteUrl(final String siteUrl) {
        try {
            set(siteUrl, SITE_URL);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name '{}': something is very wrong here.", SITE_URL, e);
        }
    }
}
