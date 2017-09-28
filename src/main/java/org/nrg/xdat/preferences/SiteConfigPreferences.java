/*
 * core: org.nrg.xdat.preferences.SiteConfigPreferences
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.preferences;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.nrg.framework.annotations.XnatMixIn;
import org.nrg.framework.beans.ProxiedBeanMixIn;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.services.NrgEventService;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.FeatureRepositoryServiceI;
import org.nrg.xdat.security.services.FeatureServiceI;
import org.nrg.xdat.security.services.RoleRepositoryServiceI;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;
import org.postgresql.util.PGInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;

/**
 * Provides property access to the site configuration preferences. Before these preferences
 * can be considered {@link #isInitialized() initialized}, you must set values for at least
 * the following preferences:
 *
 * <ul>
 *     <li>adminEmail</li>
 *     <li>archivePath</li>
 *     <li>buildPath</li>
 *     <li>cachePath</li>
 *     <li>ftpPath</li>
 *     <li>prearchivePath</li>
 *     <li>siteId</li>
 * </ul>
 */
@SuppressWarnings("unused")
@NrgPreferenceBean(toolId = SiteConfigPreferences.SITE_CONFIG_TOOL_ID,
        toolName = "XNAT Site Preferences",
        description = "Manages site configurations and settings for the XNAT system.",
        properties = "META-INF/xnat/preferences/site-config.properties",
        strict = false)
@XnatMixIn(ProxiedBeanMixIn.class)
public class SiteConfigPreferences extends EventTriggeringAbstractPreferenceBean {
    public static final String SITE_CONFIG_TOOL_ID = "siteConfig";

    @Autowired
    public SiteConfigPreferences(final NrgPreferenceService preferenceService, final NrgEventService eventService, final ConfigPaths configFolderPaths, final OrderedProperties initPrefs) {
        super(preferenceService, eventService, configFolderPaths, initPrefs);
    }

    @NrgPreference(defaultValue = "false")
    public boolean isInitialized() {
        return getBooleanValue("initialized");
    }

    public void setInitialized(final boolean initialized) {
        try {
            setBooleanValue(initialized, "initialized");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name initialized: something is very wrong here.", e);
        }
    }

    @NrgPreference
    public String getPathErrorWarning() {
        return getValue("pathErrorWarning");
    }

    public void setPathErrorWarning(final String pathErrorWarning) {
        try {
            set(pathErrorWarning, "pathErrorWarning");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name pathErrorWarning: something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "XNAT")
    public String getSiteId() {
        return getValue("siteId");
    }

    public void setSiteId(final String siteId) {
        try {
            set(siteId, "siteId");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name siteId: something is very wrong here.", e);
        }
    }

    @NrgPreference
    public String getSiteUrl() {
        return getValue("siteUrl");
    }

    public void setSiteUrl(final String siteUrl) {
        try {
            set(siteUrl, "siteUrl");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name siteUrl: something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "admin")
    public String getPrimaryAdminUsername() {
        return getValue("primaryAdminUsername");
    }

    public void setPrimaryAdminUsername(final String primaryAdminUsername) {
        try {
            set(primaryAdminUsername, "primaryAdminUsername");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'primaryAdminUsername': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "you@yoursite.org")
    public String getAdminEmail() {
        return getValue("adminEmail");
    }

    public void setAdminEmail(final String adminEmail) {
        try {
            set(adminEmail, "adminEmail");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'adminEmail': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/data/xnat/archive")
    public String getArchivePath() {
        return getValue("archivePath");
    }

    public void setArchivePath(final String archivePath) {
        try {
            set(archivePath, "archivePath");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'archivePath': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/data/xnat/prearchive")
    public String getPrearchivePath() {
        return getValue("prearchivePath");
    }

    public void setPrearchivePath(final String prearchivePath) {
        try {
            set(prearchivePath, "prearchivePath");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'prearchivePath': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/data/xnat/cache")
    public String getCachePath() {
        return getValue("cachePath");
    }

    public void setCachePath(final String cachePath) {
        try {
            set(cachePath, "cachePath");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'cachePath': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/data/xnat/ftp")
    public String getFtpPath() {
        return getValue("ftpPath");
    }

    public void setFtpPath(final String ftpPath) {
        try {
            set(ftpPath, "ftpPath");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'ftpPath': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/data/xnat/build")
    public String getBuildPath() {
        return getValue("buildPath");
    }

    public void setBuildPath(final String buildPath) {
        try {
            set(buildPath, "buildPath");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'buildPath': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/data/xnat/pipeline")
    public String getPipelinePath() {
        return getValue("pipelinePath");
    }

    public void setPipelinePath(final String pipelinePath) {
        try {
            set(pipelinePath, "pipelinePath");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'pipelinePath': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/data/xnat/inbox")
    public String getInboxPath() {
        return getValue("inboxPath");
    }

    public void setInboxPath(final String inboxPath) {
        try {
            set(inboxPath, "inboxPath");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'inboxPath': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false")
    public boolean isReloadPrearcDatabaseOnStartup() {
        return getBooleanValue("reloadPrearcDatabaseOnStartup");
    }

    public void setReloadPrearcDatabaseOnStartup(final boolean reloadPrearcDatabaseOnStartup) {
        try {
            setBooleanValue(reloadPrearcDatabaseOnStartup, "reloadPrearcDatabaseOnStartup");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'reloadPrearcDatabaseOnStartup': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "^.*$")
    public String getPasswordComplexity() {
        return getValue("passwordComplexity");
    }

    public void setPasswordComplexity(final String passwordComplexity) {
        try {
            set(passwordComplexity, "passwordComplexity");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'passwordComplexity': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "Password is not sufficiently complex.")
    public String getPasswordComplexityMessage() {
        return getValue("passwordComplexityMessage");
    }

    public void setPasswordComplexityMessage(final String passwordComplexityMessage) {
        try {
            set(passwordComplexityMessage, "passwordComplexityMessage");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'passwordComplexityMessage': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "1 year")
    public String getPasswordHistoryDuration() {
        return getValue("passwordHistoryDuration");
    }

    public void setPasswordHistoryDuration(final String passwordHistoryDuration) {
        try {
            set(passwordHistoryDuration, "passwordHistoryDuration");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'passwordHistoryDuration': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean getRequireLogin() {
        return getBooleanValue("requireLogin");
    }

    public void setRequireLogin(final boolean requireLogin) {
        try {
            setBooleanValue(requireLogin, "requireLogin");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'requireLogin': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "5 minutes")
    public String getRefreshGuestFrequency() {
        return getValue("refreshGuestFrequency");
    }

    public void setRefreshGuestFrequency(final String refreshGuestFrequency) {
        try {
            set(refreshGuestFrequency, "refreshGuestFrequency");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'refreshGuestFrequency': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean getEmailVerification() {
        return getBooleanValue("emailVerification");
    }

    public void setEmailVerification(final boolean emailVerification) {
        try {
            setBooleanValue(emailVerification, "emailVerification");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailVerification': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "Dear FULL_NAME,\n" +
                                  "<br><br>Please click this link to verify your email address: <a href=\"VERIFICATION_URL\">Verify Email</a>\n" +
                                  "<br><br>This link will expire in 24 hours.")
    public String getEmailVerificationMessage() {
        return getValue("emailVerificationMessage");
    }

    public void setEmailVerificationMessage(final String emailVerificationMessage) {
        try {
            set(emailVerificationMessage, "emailVerificationMessage");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailVerificationMessage': something is very wrong here.", e);
        }
    }

//    @NrgPreference(defaultValue = "365")
//    public int getEmailVerificationExpiration() {
//        return getIntegerValue("emailVerificationExpiration");
//    }
//
//    public void setEmailVerificationExpiration(final int emailVerificationExpiration) {
//        try {
//            setIntegerValue(emailVerificationExpiration, "emailVerificationExpiration");
//        } catch (InvalidPreferenceName e) {
//            _log.error("Invalid preference name 'emailVerificationExpiration': something is very wrong here.", e);
//        }
//    }

    @NrgPreference(defaultValue = "false")
    public boolean getUserRegistration() {
        return getBooleanValue("userRegistration");
    }

    public void setUserRegistration(final boolean userRegistration) {
        try {
            setBooleanValue(userRegistration, "userRegistration");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'userRegistration': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean getPar() {
        return getBooleanValue("par");
    }

    public void setPar(final boolean par) {
        try {
            setBooleanValue(par, "par");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'par': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false")
    public boolean getRestrictUserListAccessToAdmins() {
        return getBooleanValue("restrictUserListAccessToAdmins");
    }

    public void setRestrictUserListAccessToAdmins(final boolean restrictUserListAccessToAdmins) {
        try {
            setBooleanValue(restrictUserListAccessToAdmins, "restrictUserListAccessToAdmins");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'restrictUserListAccessToAdmins': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "['/xapi/**', '/data/**', '/REST/**', '/fs/**']")
    public List<String> getDataPaths() {
        return getListValue("dataPaths");
    }

    public void setDataPaths(final List<String> dataPaths) {
        try {
            setListValue("dataPaths", dataPaths);
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'dataPaths': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "['.*MSIE.*', '.*Mozilla.*', '.*AppleWebKit.*', '.*Opera.*']")
    public List<String> getInteractiveAgentIds() {
        return getListValue("interactiveAgentIds");
    }

    public void setInteractiveAgentIds(final List<String> interactiveAgentIds) {
        try {
            setListValue("interactiveAgentIds", interactiveAgentIds);
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'interactiveAgentIds': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean getEnableCsrfToken() {
        return getBooleanValue("enableCsrfToken");
    }

    public void setEnableCsrfToken(final boolean enableCsrfToken) {
        try {
            setBooleanValue(enableCsrfToken, "enableCsrfToken");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'enableCsrfToken': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false")
    public boolean getCsrfEmailAlert() {
        return getBooleanValue("csrfEmailAlert");
    }

    public void setCsrfEmailAlert(final boolean csrfEmailAlert) {
        try {
            setBooleanValue(csrfEmailAlert, "csrfEmailAlert");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'csrfEmailAlert': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "None")
    public String getPasswordReuseRestriction() {
        return getValue("passwordReuseRestriction");
    }

    public void setPasswordReuseRestriction(final String passwordReuseRestriction) {
        try {
            set(passwordReuseRestriction, "passwordReuseRestriction");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'passwordReuseRestriction': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false")
    public boolean getRequireSaltedPasswords() {
        return getBooleanValue("requireSaltedPasswords");
    }

    public void setRequireSaltedPasswords(final boolean requireSaltedPasswords) {
        try {
            setBooleanValue(requireSaltedPasswords, "requireSaltedPasswords");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'requireSaltedPasswords': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "Interval")
    public String getPasswordExpirationType() {
        return getValue("passwordExpirationType");
    }

    public void setPasswordExpirationType(final String passwordExpirationType) {
        try {
            set(passwordExpirationType, "passwordExpirationType");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'passwordExpirationType': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "1 year")
    public String getPasswordExpirationInterval() {
        return getValue("passwordExpirationInterval");
    }

    public void setPasswordExpirationInterval(final String passwordExpirationInterval) {
        try {
            set(passwordExpirationInterval, "passwordExpirationInterval");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'passwordExpirationInterval': something is very wrong here.", e);
        }
    }

    @NrgPreference
    public String getPasswordExpirationDate() {
        return getValue("passwordExpirationDate");
    }

    public void setPasswordExpirationDate(final String passwordExpirationDate) {
        try {
            set(passwordExpirationDate, "passwordExpirationDate");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'passwordExpirationDate': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean getEnableSitewideAnonymizationScript() {
        return getBooleanValue("enableSitewideAnonymizationScript");
    }

    public void setEnableSitewideAnonymizationScript(final boolean enableSitewideAnonymizationScript) {
        try {
            setBooleanValue(enableSitewideAnonymizationScript, "enableSitewideAnonymizationScript");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'enableSitewideAnonymizationScript': something is very wrong here.", e);
        }
    }

    @NrgPreference
    public String getSitewideAnonymizationScript() {
        return getValue("sitewideAnonymizationScript");
    }

    public void setSitewideAnonymizationScript(final String sitewideAnonymizationScript) {
        try {
            set(sitewideAnonymizationScript, "sitewideAnonymizationScript");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'sitewideAnonymizationScript': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false")
    public boolean getEnableSitewideSeriesImportFilter() {
        return getBooleanValue("enableSitewideSeriesImportFilter");
    }

    public void setEnableSitewideSeriesImportFilter(final boolean enableSitewideSeriesImportFilter) {
        try {
            setBooleanValue(enableSitewideSeriesImportFilter, "enableSitewideSeriesImportFilter");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'enableSitewideSeriesImportFilter': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "blacklist")
    public String getSitewideSeriesImportFilterMode() {
        return getValue("sitewideSeriesImportFilterMode");
    }

    public void setSitewideSeriesImportFilterMode(final String sitewideSeriesImportFilterMode) {
        try {
            set(sitewideSeriesImportFilterMode, "sitewideSeriesImportFilterMode");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'sitewideSeriesImportFilterMode': something is very wrong here.", e);
        }
    }

    @NrgPreference
    public String getSitewideSeriesImportFilter() {
        return getValue("sitewideSeriesImportFilter");
    }

    public void setSitewideSeriesImportFilter(final String sitewideSeriesImportFilter) {
        try {
            set(sitewideSeriesImportFilter, "sitewideSeriesImportFilter");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'sitewideSeriesImportFilter': something is very wrong here.", e);
        }
    }

    @NrgPreference
    public String getSitewidePetTracers() {
        return getValue("sitewidePetTracers");
    }

    public void setSitewidePetTracers(final String sitewidePetTracers) {
        try {
            set(sitewidePetTracers, "sitewidePetTracers");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'sitewidePetTracers': something is very wrong here.", e);
        }
    }

    @NrgPreference
    public String getSitewidePetMr() {
        return getValue("sitewidePetMr");
    }

    public void setSitewidePetMr(final String sitewidePetMr) {
        try {
            set(sitewidePetMr, "sitewidePetMr");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'sitewidePetMr': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean getChecksums() {
        return getBooleanValue("checksums");
    }

    public void setChecksums(final boolean checksums) {
        try {
            setBooleanValue(checksums, "checksums");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'checksums': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean getScanTypeMapping() {
        return getBooleanValue("scanTypeMapping");
    }

    public void setScanTypeMapping(final boolean scanTypeMapping) {
        try {
            setBooleanValue(scanTypeMapping, "scanTypeMapping");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'scanTypeMapping': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean isEnableDicomReceiver() {
        return getBooleanValue("enableDicomReceiver");
    }

    public void setEnableDicomReceiver(final boolean enableDicomReceiver) {
        try {
            setBooleanValue(enableDicomReceiver, "enableDicomReceiver");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'enableDicomReceiver': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "org.nrg.dcm.DicomSCPSiteConfigurationListener", aliases = "enableDicomReceiver.property.changed.listener")
    public String getEnableDicomReceiverPropertyChangedListener() {
        return getValue("enableDicomReceiverPropertyChangedListener");
    }

    public void setEnableDicomReceiverPropertyChangedListener(final String enableDicomReceiverPropertyChangedListener) {
        try {
            set(enableDicomReceiverPropertyChangedListener, "enableDicomReceiverPropertyChangedListener");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'enableDicomReceiverPropertyChangedListener': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "admin")
    public String getReceivedFileUser() {
        return getValue("receivedFileUser");
    }

    public void setReceivedFileUser(final String receivedFileUser) {
        try {
            final UserI user = Users.getUser(receivedFileUser);
            if (!Roles.isSiteAdmin(user)) {
                throw new NrgServiceRuntimeException(NrgServiceError.PermissionsViolation, receivedFileUser);
            }
            set(receivedFileUser, "receivedFileUser");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'receivedFileUser': something is very wrong here.", e);
        } catch (UserNotFoundException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.UserNotFoundError, receivedFileUser);
        } catch (UserInitException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred trying to retrieve the user " + receivedFileUser, e);
        }
    }

    @NrgPreference(defaultValue = "Session", aliases = "displayNameForGenericImageSession.singular")
    public String getImageSessionDisplayNameSingular() {
        return getValue("imageSessionDisplayNameSingular");
    }

    public void setImageSessionDisplayNameSingular(final String displayNameForGenericImageSessionSingular) {
        try {
            set(displayNameForGenericImageSessionSingular, "imageSessionDisplayNameSingular");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'imageSessionDisplayNameSingular': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "Sessions", aliases = "displayNameForGenericImageSession.plural")
    public String getImageSessionDisplayNamePlural() {
        return getValue("imageSessionDisplayNamePlural");
    }

    public void setImageSessionDisplayNamePlural(final String displayNameForGenericImageSessionPlural) {
        try {
            set(displayNameForGenericImageSessionPlural, "imageSessionDisplayNamePlural");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'imageSessionDisplayNamePlural': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", aliases = "security.require_image_assessor_labels")
    public boolean getRequireImageAssessorLabels() {
        return getBooleanValue("requireImageAssessorLabels");
    }

    public void setRequireImageAssessorLabels(final boolean requireImageAssessorLabels) throws InvalidPreferenceName {
        setBooleanValue(requireImageAssessorLabels, "requireImageAssessorLabels");
    }

    @NrgPreference(defaultValue = "zip,jar,rar,ear,gar,mrb")
    public String getZipExtensions() {
        return getValue("zipExtensions");
    }


    // just the extensions.  not the delimiter too.
    public String[] getZipExtensionsAsArray() {
        return getValue("zipExtensions").split("\\s*,\\s*");
    }

    public void setZipExtensions(final String zipExtensions) {
        try {
            set("zipExtensions", zipExtensions);
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'zipExtensions': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/images/logo.png")
    public String getSiteLogoPath() {
        return getValue("siteLogoPath");
    }

    public void setSiteLogoPath(final String siteLogoPath) {
        try {
            set(siteLogoPath, "siteLogoPath");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'siteLogoPath': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "Text")
    public String getSiteDescriptionType() {
        return getValue("siteDescriptionType");
    }

    public void setSiteDescriptionType(final String siteDescriptionType) {
        try {
            set(siteDescriptionType, "siteDescriptionType");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'siteDescriptionType': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/screens/site_description.vm")
    public String getSiteDescriptionPage() {
        return getValue("siteDescriptionPage");
    }

    public void setSiteDescriptionPage(final String siteDescriptionPage) {
        try {
            set(siteDescriptionPage, "siteDescriptionPage");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'siteDescriptionPage': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "Welcome to **XNAT**: You can change this site description by clicking the **Administer->Site Administration** menu option, and modifying the **Site Description** setting on the **Site Setup** tab.")
    public String getSiteDescriptionText() {
        return getValue("siteDescriptionText");
    }

    public void setSiteDescriptionText(final String siteDescriptionText) {
        try {
            set(siteDescriptionText, "siteDescriptionText");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'siteDescriptionText': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/screens/QuickSearch.vm")
    public String getSiteLoginLanding() {
        return getValue("siteLoginLanding");
    }

    public void setSiteLoginLanding(final String siteLoginLanding) {
        try {
            set(siteLoginLanding, "siteLoginLanding");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'siteLoginLanding': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/Index.vm")
    public String getSiteLandingLayout() {
        return getValue("siteLandingLayout");
    }

    public void setSiteLandingLayout(final String siteLandingLayout) {
        try {
            set(siteLandingLayout, "siteLandingLayout");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'siteLandingLayout': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/screens/QuickSearch.vm")
    public String getSiteHome() {
        return getValue("siteHome");
    }

    public void setSiteHome(final String siteHome) {
        try {
            set(siteHome, "siteHome");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'siteHome': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "any", aliases = "security.channel")
    public String getSecurityChannel() {
        return getValue("securityChannel");
    }

    public void setSecurityChannel(final String channel) {
        try {
            set(channel, "securityChannel");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'securityChannel': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false")
    public boolean getMatchSecurityProtocol() {
        return getBooleanValue("matchSecurityProtocol");
    }

    public void setMatchSecurityProtocol(final boolean matchSecurityProtocol) {
        try {
            setBooleanValue(matchSecurityProtocol, "matchSecurityProtocol");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'matchSecurityProtocol': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", aliases = "security.allow-data-admins")
    public boolean getAllowDataAdmins() {
        return getBooleanValue("allowDataAdmins");
    }

    public void setAllowDataAdmins(final boolean allowDataAdmins) {
        try {
            setBooleanValue(allowDataAdmins, "allowDataAdmins");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'allowDataAdmins': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "[]", aliases = "security.fail_merge_on")
    public List<String> getFailMergeOn() {
        return getListValue("failMergeOn");
    }

    public String getFormattedFailMergeOn() {
        final List<String> values = getListValue("failMergeOn");
        if (values == null || values.size() == 0) {
            return "[]";
        }
        return "['" + Joiner.on("', '").join(values) + "']";
    }

    public void setFailMergeOn(final List<String> failMergeOn) {
        try {
            setListValue("failMergeOn", failMergeOn);
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'failMergeOn': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "1000", aliases = "sessions.concurrent_max")
    public int getConcurrentMaxSessions() {
        return getIntegerValue("concurrentMaxSessions");
    }

    public void setConcurrentMaxSessions(final int concurrentMaxSessions) {
        try {
            setIntegerValue(concurrentMaxSessions, "concurrentMaxSessions");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'concurrentMaxSessions': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/Index.vm")
    public String getSiteHomeLayout() {
        return getValue("siteHomeLayout");
    }

    public void setSiteHomeLayout(final String siteHomeLayout) {
        try {
            set(siteHomeLayout, "siteHomeLayout");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'siteHomeLayout': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "0")
    public int getSiteWideAlertStatus() {
        return getIntegerValue("siteWideAlertStatus");
    }

    public void setSiteWideAlertStatus(final int siteWideAlertStatus) {
        try {
            setIntegerValue(siteWideAlertStatus, "siteWideAlertStatus");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'siteWideAlertStatus': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "message")
    public String getSiteWideAlertType() {
        return getValue("siteWideAlertType");
    }

    public void setSiteWideAlertType(final String siteWideAlertType) {
        try {
            set(siteWideAlertType, "siteWideAlertType");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'siteWideAlertType': something is very wrong here.", e);
        }
    }

    @NrgPreference
    public String getSiteWideAlertMessage() {
        return getValue("siteWideAlertMessage");
    }

    public void setSiteWideAlertMessage(final String siteWideAlertMessage) {
        try {
            set(siteWideAlertMessage, "siteWideAlertMessage");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'siteWideAlertMessage': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", aliases = "UI.debug-extension-points")
    public boolean getUiDebugExtensionPoints() {
        return getBooleanValue("uiDebugExtensionPoints");
    }

    public void setUiDebugExtensionPoints(final boolean uiDebugExtensionPoints) {
        try {
            setBooleanValue(uiDebugExtensionPoints, "uiDebugExtensionPoints");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'uiDebugExtensionPoints': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", aliases = "UI.display-series-description")
    public boolean getUiDisplaySeriesDescription() {
        return getBooleanValue("uiDisplaySeriesDescription");
    }

    public void setUiDisplaySeriesDescription(final boolean uiDisplaySeriesDescription) {
        try {
            setBooleanValue(uiDisplaySeriesDescription, "uiDisplaySeriesDescription");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'uiDisplaySeriesDescription': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", aliases = "UI.allow-advanced-search")
    public boolean getUiAllowAdvancedSearch() {
        return getBooleanValue("uiAllowAdvancedSearch");
    }

    public void setUiAllowAdvancedSearch(final boolean uiAllowAdvancedSearch) {
        try {
            setBooleanValue(uiAllowAdvancedSearch, "uiAllowAdvancedSearch");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'uiAllowAdvancedSearch': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", aliases = "UI.allow-project-delete")
    public boolean getUiAllowProjectDelete() {
        return getBooleanValue("uiAllowProjectDelete");
    }

    public void setUiAllowProjectDelete(final boolean uiAllowProjectDelete) {
        try {
            setBooleanValue(uiAllowProjectDelete, "uiAllowProjectDelete");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'uiAllowProjectDelete': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", aliases = "UI.allow-new-user-comments")
    public boolean getUiAllowNewUserComments() {
        return getBooleanValue("uiAllowNewUserComments");
    }

    public void setUiAllowNewUserComments(final boolean uiAllowNewUserComments) {
        try {
            setBooleanValue(uiAllowNewUserComments, "uiAllowNewUserComments");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'uiAllowNewUserComments': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", aliases = "UI.allow-scan-addition")
    public boolean getUiAllowScanAddition() {
        return getBooleanValue("uiAllowScanAddition");
    }

    public void setUiAllowScanAddition(final boolean uiAllowScanAddition) {
        try {
            setBooleanValue(uiAllowScanAddition, "uiAllowScanAddition");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'uiAllowScanAddition': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", aliases = "project.allow-auto-archive")
    public boolean getProjectAllowAutoArchive() {
        return getBooleanValue("projectAllowAutoArchive");
    }

    public void setProjectAllowAutoArchive(final boolean projectAllowAutoArchive) {
        try {
            setBooleanValue(projectAllowAutoArchive, "projectAllowAutoArchive");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'projectAllowAutoArchive': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", aliases = "UI.allow-quarantine")
    public boolean getUiAllowQuarantine() {
        return getBooleanValue("uiAllowQuarantine");
    }

    public void setUiAllowQuarantine(final boolean uiAllowQuarantine) {
        try {
            setBooleanValue(uiAllowQuarantine, "uiAllowQuarantine");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'uiAllowQuarantine': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", aliases = "UI.allow-scan-type-modification")
    public boolean getUiAllowScanTypeModification() {
        return getBooleanValue("uiAllowScanTypeModification");
    }

    public void setUiAllowScanTypeModification(final boolean uiAllowScanTypeModification) {
        try {
            setBooleanValue(uiAllowScanTypeModification, "uiAllowScanTypeModification");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'uiAllowScanTypeModification': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", aliases = "UI.show-left-bar")
    public boolean getUiShowLeftBar() {
        return getBooleanValue("uiShowLeftBar");
    }

    public void setUiShowLeftBar(final boolean uiShowLeftBar) {
        try {
            setBooleanValue(uiShowLeftBar, "uiShowLeftBar");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'uiShowLeftBar': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", aliases = "UI.show-left-bar-projects")
    public boolean getUiShowLeftBarProjects() {
        return getBooleanValue("uiShowLeftBarProjects");
    }

    public void setUiShowLeftBarProjects(final boolean uiShowLeftBarProjects) {
        try {
            setBooleanValue(uiShowLeftBarProjects, "uiShowLeftBarProjects");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'uiShowLeftBarProjects': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", aliases = "UI.show-left-bar-favorites")
    public boolean getUiShowLeftBarFavorites() {
        return getBooleanValue("uiShowLeftBarFavorites");
    }

    public void setUiShowLeftBarFavorites(final boolean uiShowLeftBarFavorites) {
        try {
            setBooleanValue(uiShowLeftBarFavorites, "uiShowLeftBarFavorites");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'uiShowLeftBarFavorites': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", aliases = "UI.show-left-bar-search")
    public boolean getUiShowLeftBarSearch() {
        return getBooleanValue("uiShowLeftBarSearch");
    }

    public void setUiShowLeftBarSearch(final boolean uiShowLeftBarSearch) {
        try {
            setBooleanValue(uiShowLeftBarSearch, "uiShowLeftBarSearch");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'uiShowLeftBarSearch': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", aliases = "UI.show-left-bar-browse")
    public boolean getUiShowLeftBarBrowse() {
        return getBooleanValue("uiShowLeftBarBrowse");
    }

    public void setUiShowLeftBarBrowse(final boolean uiShowLeftBarBrowse) {
        try {
            setBooleanValue(uiShowLeftBarBrowse, "UiShowLeftBarBrowse");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'UiShowLeftBarBrowse': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", aliases = "UI.show-manage-files")
    public boolean getUiShowManageFiles() {
        return getBooleanValue("uiShowManageFiles");
    }

    public void setUiShowManageFiles(final boolean uiShowManageFiles) {
        try {
            setBooleanValue(uiShowManageFiles, "uiShowManageFiles");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'uiShowManageFiles': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", aliases = "UI.show-project-manage-files")
    public boolean getUiShowProjectManageFiles() {
        return getBooleanValue("uiShowProjectManageFiles");
    }

    public void setUiShowProjectManageFiles(final boolean uiShowProjectManageFiles) {
        try {
            setBooleanValue(uiShowProjectManageFiles, "uiShowProjectManageFiles");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'uiShowProjectManageFiles': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", aliases = "UI.allow-subject-create-from-expt-edit")
    public boolean getUiAllowSubjectCreateFromExptEdit() {
        return getBooleanValue("uiAllowSubjectCreateFromExptEdit");
    }

    public void setUiAllowSubjectCreateFromExptEdit(final boolean uiAllowSubjectCreateFromExptEdit) {
        try {
            setBooleanValue(uiAllowSubjectCreateFromExptEdit, "uiAllowSubjectCreateFromExptEdit");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'uiAllowSubjectCreateFromExptEdit': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", aliases = "UI.allow-non-admin-project-creation")
    public boolean getUiAllowNonAdminProjectCreation() {
        return getBooleanValue("uiAllowNonAdminProjectCreation");
    }

    public void setUiAllowNonAdminProjectCreation(final boolean uiAllowNonAdminProjectCreation) {
        try {
            setBooleanValue(uiAllowNonAdminProjectCreation, "uiAllowNonAdminProjectCreation");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'uiAllowNonAdminProjectCreation': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean getAllowNonAdminsToClaimUnassignedSessions() {
        return getBooleanValue("allowNonAdminsToClaimUnassignedSessions");
    }

    public void setAllowNonAdminsToClaimUnassignedSessions(final boolean allowNonAdminsToClaimUnassignedSessions) {
        try {
            setBooleanValue(allowNonAdminsToClaimUnassignedSessions, "allowNonAdminsToClaimUnassignedSessions");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'allowNonAdminsToClaimUnassignedSessions': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "^.*$")
    public String getIpsThatCanSendEmailsThroughRest() {
        return getValue("ipsThatCanSendEmailsThroughRest");
    }

    public void setIpsThatCanSendEmailsThroughRest(final String ipsThatCanSendEmailsThroughRest) {
        try {
            set(ipsThatCanSendEmailsThroughRest, "ipsThatCanSendEmailsThroughRest");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'ipsThatCanSendEmailsThroughRest': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "Your login attempt failed because the username and password combination you provided was invalid or your user already has the maximum number of user sessions open. After %d failed login attempts, your user account will be locked. If you believe your account is currently locked, you can:<ul><li>Unlock it by resetting your password</li><li>Wait one hour for it to unlock automatically</li></ul>",
            aliases = "UI.login_failure_message")
    public String getUiLoginFailureMessage() {
        return getValue("uiLoginFailureMessage");
    }

    public void setUiLoginFailureMessage(final String uiLoginFailureMessage) {
        try {
            set(uiLoginFailureMessage, "uiLoginFailureMessage");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'uiLoginFailureMessage': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", aliases = "UI.allow-blocked-subject-assessor-view")
    public boolean getUiAllowBlockedSubjectAssessorView() {
        return getBooleanValue("uiAllowBlockedSubjectAssessorView");
    }

    public void setUiAllowBlockedSubjectAssessorView(final boolean uiAllowBlockedSubjectAssessorView) {
        try {
            setBooleanValue(uiAllowBlockedSubjectAssessorView, "uiAllowBlockedSubjectAssessorView");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'uiAllowBlockedSubjectAssessorView': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = FeatureServiceI.DEFAULT_FEATURE_SERVICE, aliases = "security.services.feature.default")
    public String getFeatureService() {
        return getValue("featureService");
    }

    public void setFeatureService(final String featureService) {
        try {
            set(featureService, "featureService");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'featureService': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = FeatureRepositoryServiceI.DEFAULT_FEATURE_REPO_SERVICE, aliases = "security.services.featureRepository.default")
    public String getFeatureRepositoryService() {
        return getValue("featureRepositoryService");
    }

    public void setFeatureRepositoryService(final String featureRepositoryService) {
        try {
            set(featureRepositoryService, "featureRepositoryService");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'featureRepositoryService': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = RoleServiceI.DEFAULT_ROLE_SERVICE, aliases = "security.services.role.default")
    public String getRoleService() {
        return getValue("roleService");
    }

    public void setRoleService(final String roleService) {
        try {
            set(roleService, "roleService");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'roleService': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = RoleRepositoryServiceI.DEFAULT_ROLE_REPO_SERVICE, aliases = "security.services.roleRepository.default")
    public String getRoleRepositoryService() {
        return getValue("roleRepositoryService");
    }

    public void setRoleRepositoryService(final String roleRepositoryService) {
        try {
            set(roleRepositoryService, "roleRepositoryService");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'roleRepositoryService': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", aliases = "security.allow-HTML-resource-rendering")
    public boolean getAllowHtmlResourceRendering() {
        return getBooleanValue("allowHtmlResourceRendering");
    }

    public void setAllowHtmlResourceRendering(final String allowHtmlResourceRendering) {
        try {
            set(allowHtmlResourceRendering, "allowHtmlResourceRendering");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'allowHtmlResourceRendering': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "20")
    public int getMaxFailedLogins() {
        return getIntegerValue("maxFailedLogins");
    }

    public void setMaxFailedLogins(final int maxFailedLogins) {
        try {
            setIntegerValue(maxFailedLogins, "maxFailedLogins");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'maxFailedLogins': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "1 hour")
    public String getMaxFailedLoginsLockoutDuration() {
        return getValue("maxFailedLoginsLockoutDuration");
    }

    public void setMaxFailedLoginsLockoutDuration(final String maxFailedLoginsLockoutDuration) {
        try {
            set(maxFailedLoginsLockoutDuration, "maxFailedLoginsLockoutDuration");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'maxFailedLoginsLockoutDuration': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "0 0 * * * *") // 0 0 * * * * means it runs every hour
    public String getResetFailedLoginsSchedule() {
        return getValue("resetFailedLoginsSchedule");
    }

    public void setResetFailedLoginsSchedule(final String resetFailedLoginsSchedule) {
        try {
            set(resetFailedLoginsSchedule, "resetFailedLoginsSchedule");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'resetFailedLoginsSchedule': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean getCanResetFailedLoginsWithForgotPassword() {
        return getBooleanValue("canResetFailedLoginsWithForgotPassword");
    }

    public void setCanResetFailedLoginsWithForgotPassword(final String canResetFailedLoginsWithForgotPassword) {
        try {
            set(canResetFailedLoginsWithForgotPassword, "canResetFailedLoginsWithForgotPassword");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'canResetFailedLoginsWithForgotPassword': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "1 year")
    public String getInactivityBeforeLockout() {
        return getValue("inactivityBeforeLockout");
    }

    public void setInactivityBeforeLockout(final String inactivityBeforeLockout) {
        try {
            set(inactivityBeforeLockout, "inactivityBeforeLockout");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'inactivityBeforeLockout': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "0 0 1 * * ?") // 0 0 1 * * ? means it runs at 1AM every day
    public String getInactivityBeforeLockoutSchedule() {
        return getValue("inactivityBeforeLockoutSchedule");
    }

    public void setInactivityBeforeLockoutSchedule(final String inactivityBeforeLockoutSchedule) {
        try {
            set(inactivityBeforeLockoutSchedule, "inactivityBeforeLockoutSchedule");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'inactivityBeforeLockoutSchedule': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "60000")
    public long getSessionXmlRebuilderRepeat() {
        return getLongValue("sessionXmlRebuilderRepeat");
    }

    public void setSessionXmlRebuilderRepeat(final long sessionXmlRebuilderRepeat) {
        try {
            setLongValue(sessionXmlRebuilderRepeat, "sessionXmlRebuilderRepeat");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'sessionXmlRebuilderRepeat': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "5")
    public int getSessionXmlRebuilderInterval() {
        return getIntegerValue("sessionXmlRebuilderInterval");
    }

    public void setSessionXmlRebuilderInterval(final int sessionXmlRebuilderInterval) {
        try {
            setIntegerValue(sessionXmlRebuilderInterval, "sessionXmlRebuilderInterval");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'sessionXmlRebuilderInterval': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "15 minutes")
    public String getSessionTimeout() {
        return getValue("sessionTimeout");
    }

    public void setSessionTimeout(final String sessionTimeout) {
        try {
            set(sessionTimeout, "sessionTimeout");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'sessionTimeout': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "2 days")
    public String getAliasTokenTimeout() {
        return getValue("aliasTokenTimeout");
    }

    public void setAliasTokenTimeout(final String aliasTokenTimeout) {
        try {
            set(aliasTokenTimeout, "aliasTokenTimeout");

        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'aliasTokenTimeout': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "0 0 * * * *") // 0 0 * * * * means it runs every hour
    public String getAliasTokenTimeoutSchedule() {
        return getValue("aliasTokenTimeoutSchedule");
    }

    public void setAliasTokenTimeoutSchedule(final String aliasTokenTimeoutSchedule) {
        try {
            set(aliasTokenTimeoutSchedule, "aliasTokenTimeoutSchedule");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'aliasTokenTimeoutSchedule': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "Session timed out at TIMEOUT_TIME.")
    public String getSessionTimeoutMessage() {
        return getValue("sessionTimeoutMessage");
    }

    public void setSessionTimeoutMessage(final String sessionTimeoutMessage) {
        try {
            set(sessionTimeoutMessage, "sessionTimeoutMessage");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'sessionTimeoutMessage': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", aliases = "audit.show_change_justification")
    public boolean getShowChangeJustification() {
        return getBooleanValue("showChangeJustification");
    }

    public void setShowChangeJustification(final boolean showChangeJustification) {
        try {
            setBooleanValue(showChangeJustification, "showChangeJustification");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'showChangeJustification': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", aliases = "audit.require_change_justification")
    public boolean getRequireChangeJustification() {
        return getBooleanValue("requireChangeJustification");
    }

    public void setRequireChangeJustification(final boolean requireChangeJustification) {
        try {
            setBooleanValue(requireChangeJustification, "requireChangeJustification");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'requireChangeJustification': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", aliases = "audit.require_event_name")
    public boolean getRequireEventName() {
        return getBooleanValue("requireEventName");
    }

    public void setRequireEventName(final boolean requireEventName) {
        try {
            setBooleanValue(requireEventName, "requireEventName");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'requireEventName': something is very wrong here.", e);
        }
    }

    @JsonIgnore
    public static long convertPGIntervalToSeconds(final String expression) throws SQLException {
        final PGInterval interval = new PGInterval(expression);
        return ((long) interval.getYears()) * 31536000L +
               ((long) interval.getMonths()) * 2592000L +
               ((long) interval.getDays()) * 86400L +
               ((long) interval.getHours()) * 3600L +
               ((long) interval.getMinutes()) * 60L +
               ((long) interval.getSeconds());
    }

    public boolean isComplete() {
        return getMissingInitSettings().size() == 0;
    }

    /**
     * Overrides default {@link AbstractPreferenceBean#postProcessPreferences()} method to add automatically setting
     * the {@link #isInitialized() initialized} setting to true if the site configuration preferences were initialized
     * from prefs-init.ini or prefs-override.ini (indicated by {@link AbstractPreferenceBean#isInitFromConfig()} value).
     */
    @Override
    protected void postProcessPreferences() {
        if (isInitFromConfig()) {
            final List<String> missing = getMissingInitSettings();
            if (missing.size() == 0) {
                setInitialized(true);
            } else {
                _log.warn("Your configuration was initialized from a configuration file, but the following settings were not initialized: {}. These must be set before the initialization configuration for the system can be fully initialized.", Joiner.on(", ").join(missing));
                setInitialized(false);
            }
        }
    }

    @NotNull
    private List<String> getMissingInitSettings() {
        final List<String> missing = Lists.newArrayList();
        if (StringUtils.isBlank(getSiteId())) {
            missing.add("siteId");
        }
        if (StringUtils.isBlank(getSiteUrl())) {
            missing.add("siteUrl");
        }
        if (StringUtils.isBlank(getAdminEmail())) {
            missing.add("adminEmail");
        }
        if (StringUtils.isBlank(getArchivePath())) {
            missing.add("archivePath");
        }
        if (StringUtils.isBlank(getPrearchivePath())) {
            missing.add("prearchivePath");
        }
        if (StringUtils.isBlank(getCachePath())) {
            missing.add("cachePath");
        }
        if (StringUtils.isBlank(getBuildPath())) {
            missing.add("buildPath");
        }
        if (StringUtils.isBlank(getFtpPath())) {
            missing.add("ftpPath");
        }
        return missing;
    }

    private static final Logger _log = LoggerFactory.getLogger(SiteConfigPreferences.class);
}
