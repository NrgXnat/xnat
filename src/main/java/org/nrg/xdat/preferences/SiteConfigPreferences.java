package org.nrg.xdat.preferences;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.xdat.security.services.FeatureRepositoryServiceI;
import org.nrg.xdat.security.services.FeatureServiceI;
import org.nrg.xdat.security.services.RoleRepositoryServiceI;
import org.nrg.xdat.security.services.RoleServiceI;
import org.postgresql.util.PGInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlRootElement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@XmlRootElement
@NrgPreferenceBean(toolId = SiteConfigPreferences.SITE_CONFIG_TOOL_ID,
                   toolName = "XNAT Site Preferences",
                   description = "Manages site configurations and settings for the XNAT system.",
                   properties = "config/site/siteConfiguration.properties",
                   strict = false)
public class SiteConfigPreferences extends AbstractPreferenceBean {
    public static final String SITE_CONFIG_TOOL_ID = "siteConfig";

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

    @NrgPreference(defaultValue = "{'host':'localhost','port':'25'}")
    public Map<String, String> getSmtpServer() {
        return getMapValue("smtpServer");
    }

    public void setSmtpServer(final Map<String, String> smtpServer) {
        try {
            setMapValue("smtpServer", smtpServer);
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'smtpServer': something is very wrong here.", e);
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

    @NrgPreference(defaultValue = "365")
    public int getPasswordHistoryDuration() {
        return getIntegerValue("passwordHistoryDuration");
    }

    public void setPasswordHistoryDuration(final int passwordHistoryDuration) {
        try {
            setIntegerValue(passwordHistoryDuration, "passwordHistoryDuration");
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

    @NrgPreference(defaultValue = "true")
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

    @NrgPreference(defaultValue = "true")
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

    @NrgPreference(defaultValue = "true")
    public boolean getEmailAllowNonuserSubscribers() {
        return getBooleanValue("emailAllowNonuserSubscribers");
    }

    public void setEmailAllowNonuserSubscribers(final boolean emailAllowNonuserSubscribers) {
        try {
            setBooleanValue(emailAllowNonuserSubscribers, "emailAllowNonuserSubscribers");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailAllowNonuserSubscribers': something is very wrong here.", e);
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

    @NrgPreference(defaultValue = "365")
    public int getPasswordExpirationInterval() {
        return getIntegerValue("passwordExpirationInterval");
    }

    public void setPasswordExpirationInterval(final int passwordExpirationInterval) {
        try {
            setIntegerValue(passwordExpirationInterval, "passwordExpirationInterval");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'passwordExpirationInterval': something is very wrong here.", e);
        }
    }

    @NrgPreference
    public Date getPasswordExpirationDate() {
        return getDateValue("passwordExpirationDate");
    }

    public void setPasswordExpirationDate(final Date passwordExpirationDate) {
        try {
            setDateValue(passwordExpirationDate, "passwordExpirationDate");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'passwordExpirationDate': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false")
    public boolean getShowApplet() {
        return getBooleanValue("showApplet");
    }

    public void setShowApplet(final boolean showApplet) {
        try {
            setBooleanValue(showApplet, "showApplet");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'showApplet': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false")
    public boolean getEnableProjectAppletScript() {
        return getBooleanValue("enableProjectAppletScript");
    }

    public void setEnableProjectAppletScript(final boolean enableProjectAppletScript) {
        try {
            setBooleanValue(enableProjectAppletScript, "enableProjectAppletScript");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'enableProjectAppletScript': something is very wrong here.", e);
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

    @NrgPreference(defaultValue = "true", property = "smtp.enabled")
    public boolean getSmtpEnabled() {
        return getBooleanValue("smtp.enabled");
    }

    public void setSmtpEnabled(final boolean smtpEnabled) {
        try {
            setBooleanValue(smtpEnabled, "smtp.enabled");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'smtp.enabled': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "org.nrg.xnat.utils.ChecksumsSiteConfigurationListener", property = "checksums.property.changed.listener")
    public String getChecksumsPropertyChangedListener() {
        return getValue("checksums.property.changed.listener");
    }

    public void setChecksumsPropertyChangedListener(final String checksumsPropertyChangedListener) {
        try {
            set(checksumsPropertyChangedListener, "checksums.property.changed.listener");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'checksums.property.changed.listener': something is very wrong here.", e);
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

    @NrgPreference(defaultValue = "org.nrg.dcm.DicomSCPSiteConfigurationListener", property = "enableDicomReceiver.property.changed.listener")
    public String getEnableDicomReceiverPropertyChangedListener() {
        return getValue("enableDicomReceiver.property.changed.listener");
    }

    public void setEnableDicomReceiverPropertyChangedListener(final String enableDicomReceiverPropertyChangedListener) {
        try {
            set(enableDicomReceiverPropertyChangedListener, "enableDicomReceiver.property.changed.listener");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'enableDicomReceiver.property.changed.listener': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "admin")
    public String getReceivedFileUser() {
        return getValue("receivedFileUser");
    }

    public void setReceivedFileUser(final String receivedFileUser) {
        try {
            set(receivedFileUser, "receivedFileUser");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'receivedFileUser': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "Session", property = "displayNameForGenericImageSession.singular")
    public String getImageSessionDisplayNameSingular() {
        return getValue("displayNameForGenericImageSession.singular");
    }

    public void setImageSessionDisplayNameSingular(final String displayNameForGenericImageSessionSingular) {
        try {
            set(displayNameForGenericImageSessionSingular, "displayNameForGenericImageSession.singular");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'displayNameForGenericImageSession.singular': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "Sessions", property = "displayNameForGenericImageSession.plural")
    public String getImageSessionDisplayNamePlural() {
        return getValue("displayNameForGenericImageSession.plural");
    }

    public void setImageSessionDisplayNamePlural(final String displayNameForGenericImageSessionPlural) {
        try {
            set(displayNameForGenericImageSessionPlural, "displayNameForGenericImageSession.plural");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'displayNameForGenericImageSession.plural': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", property = "security.require_image_assessor_labels")
    public boolean getRequireImageAssessorLabels() {
        return getBooleanValue("security.require_image_assessor_labels");
    }

    public void setRequireImageAssessorLabels(final boolean requireImageAssessorLabels) throws InvalidPreferenceName {
        setBooleanValue(requireImageAssessorLabels, "security.require_image_assessor_labels");
    }

    @NrgPreference(defaultValue = "['zip','jar','rar','ear','gar','mrb']")
    public List<String> getZipExtensions() {
        return getListValue("zipExtensions");
    }

    public void setZipExtensions(final List<String> zipExtensions) {
        try {
            setListValue("zipExtensions", zipExtensions);
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'zipExtensions': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "Page")
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

    @NrgPreference(defaultValue = "SITE DESCRIPTION HERE: Go to Administer -> Configuration -> Site Information to change.")
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

    @NrgPreference(defaultValue = "any", property = "security.channel")
    public String getSecurityChannel() {
        return getValue("security.channel");
    }

    public void setSecurityChannel(final String channel) {
        try {
            set(channel, "security.channel");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'security.channel': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "1000", property = "sessions.concurrent_max")
    public int getConcurrentMaxSessions() {
        return getIntegerValue("sessions.concurrent_max");
    }

    public void setConcurrentMaxSessions(final int concurrentMaxSessions) {
        try {
            setIntegerValue(concurrentMaxSessions, "sessions.concurrent_max");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'sessions.concurrent_max': something is very wrong here.", e);
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

    @NrgPreference(defaultValue = "false", property = "UI.debug-extension-points")
    public boolean getUiDebugExtensionPoints() {
        return getBooleanValue("UI.debug-extension-points");
    }

    public void setUiDebugExtensionPoints(final boolean uiDebugExtensionPoints) {
        try {
            setBooleanValue(uiDebugExtensionPoints, "UI.debug-extension-points");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'UI.debug-extension-points': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", property = "UI.allow-advanced-search")
    public boolean getUiAllowAdvancedSearch() {
        return getBooleanValue("UI.allow-advanced-search");
    }

    public void setUiAllowAdvancedSearch(final boolean uiAllowAdvancedSearch) {
        try {
            setBooleanValue(uiAllowAdvancedSearch, "UI.allow-advanced-search");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'UI.allow-advanced-search': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", property = "UI.allow-new-user-comments")
    public boolean getUiAllowNewUserComments() {
        return getBooleanValue("UI.allow-new-user-comments");
    }

    public void setUiAllowNewUserComments(final boolean uiAllowNewUserComments) {
        try {
            setBooleanValue(uiAllowNewUserComments, "UI.allow-new-user-comments");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'UI.allow-new-user-comments': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", property = "UI.allow-scan-addition")
    public boolean getUiAllowScanAddition() {
        return getBooleanValue("UI.allow-scan-addition");
    }

    public void setUiAllowScanAddition(final boolean uiAllowScanAddition) {
        try {
            setBooleanValue(uiAllowScanAddition, "UI.allow-scan-addition");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'UI.allow-scan-addition': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", property = "UI.show-left-bar")
    public boolean getUiShowLeftBar() {
        return getBooleanValue("UI.show-left-bar");
    }

    public void setUiShowLeftBar(final boolean uiShowLeftBar) {
        try {
            setBooleanValue(uiShowLeftBar, "UI.show-left-bar");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'UI.show-left-bar': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", property = "UI.show-left-bar-projects")
    public boolean getUiShowLeftBarProjects() {
        return getBooleanValue("UI.show-left-bar-projects");
    }

    public void setUiShowLeftBarProjects(final boolean uiShowLeftBarProjects) {
        try {
            setBooleanValue(uiShowLeftBarProjects, "UI.show-left-bar-projects");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'UI.show-left-bar-projects': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", property = "UI.show-left-bar-favorites")
    public boolean getUiShowLeftBarFavorites() {
        return getBooleanValue("UI.show-left-bar-favorites");
    }

    public void setUiShowLeftBarFavorites(final boolean uiShowLeftBarFavorites) {
        try {
            setBooleanValue(uiShowLeftBarFavorites, "UI.show-left-bar-favorites");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'UI.show-left-bar-favorites': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", property = "UI.show-left-bar-search")
    public boolean getUiShowLeftBarSearch() {
        return getBooleanValue("UI.show-left-bar-search");
    }

    public void setUiShowLeftBarSearch(final boolean uiShowLeftBarSearch) {
        try {
            setBooleanValue(uiShowLeftBarSearch, "UI.show-left-bar-search");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'UI.show-left-bar-search': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", property = "UI.show-left-bar-browse")
    public boolean getUiShowLeftBarBrowse() {
        return getBooleanValue("UI.show-left-bar-browse");
    }

    public void setUiShowLeftBarBrowse(final boolean uiShowLeftBarBrowse) {
        try {
            setBooleanValue(uiShowLeftBarBrowse, "UI.show-left-bar-browse");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'UI.show-left-bar-browse': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", property = "UI.show-manage-files")
    public boolean getUiShowManageFiles() {
        return getBooleanValue("UI.show-manage-files");
    }

    public void setUiShowManageFiles(final boolean uiShowManageFiles) {
        try {
            setBooleanValue(uiShowManageFiles, "UI.show-manage-files");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'UI.show-manage-files': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", property = "UI.allow-non-admin-project-creation")
    public boolean getUiAllowNonAdminProjectCreation() {
        return getBooleanValue("UI.allow-non-admin-project-creation");
    }

    public void setUiAllowNonAdminProjectCreation(final boolean uiAllowNonAdminProjectCreation) {
        try {
            setBooleanValue(uiAllowNonAdminProjectCreation, "UI.allow-non-admin-project-creation");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'UI.allow-non-admin-project-creation': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "Your login attempt failed because the username and password combination you provided was invalid. After %d failed login attempts, your user account will be locked. If you believe your account is currently locked, you can:<ul><li>Unlock it by resetting your password</li><li>Wait one hour for it to unlock automatically</li></ul>", property = "UI.login_failure_message")
    public String getUiLoginFailureMessage() {
        return getValue("UI.login_failure_message");
    }

    public void setUiLoginFailureMessage(final String uiLoginFailureMessage) {
        try {
            set(uiLoginFailureMessage, "UI.login_failure_message");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'UI.login_failure_message': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", property = "UI.allow-blocked-subject-assessor-view")
    public boolean getUiAllowBlockedSubjectAssessorView() {
        return getBooleanValue("UI.allow-blocked-subject-assessor-view");
    }

    public void setUiAllowBlockedSubjectAssessorView(final boolean uiAllowBlockedSubjectAssessorView) {
        try {
            setBooleanValue(uiAllowBlockedSubjectAssessorView, "UI.allow-blocked-subject-assessor-view");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'UI.allow-blocked-subject-assessor-view': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = FeatureServiceI.DEFAULT_FEATURE_SERVICE, property = "security.services.feature.default")
    public String getFeatureService() {
        return getValue("security.services.feature.default");
    }

    public void setFeatureService(final String featureService) {
        try {
            set(featureService, "security.services.feature.default");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'security.services.feature.default': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = FeatureRepositoryServiceI.DEFAULT_FEATURE_REPO_SERVICE, property = "security.services.featureRepository.default")
    public String getFeatureRepositoryService() {
        return getValue("security.services.featureRepository.default");
    }

    public void setFeatureRepositoryService(final String featureRepositoryService) {
        try {
            set(featureRepositoryService, "security.services.featureRepository.default");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'security.services.featureRepository.default': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = RoleServiceI.DEFAULT_ROLE_SERVICE, property = "security.services.role.default")
    public String getRoleService() {
        return getValue("security.services.role.default");
    }

    public void setRoleService(final String roleService) {
        try {
            set(roleService, "security.services.role.default");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'security.services.role.default': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = RoleRepositoryServiceI.DEFAULT_ROLE_REPO_SERVICE, property = "security.services.roleRepository.default")
    public String getRoleRepositoryService() {
        return getValue("security.services.roleRepository.default");
    }

    public void setRoleRepositoryService(final String roleRepositoryService) {
        try {
            set(roleRepositoryService, "security.services.roleRepository.default");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'security.services.roleRepository.default': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true", property = "security.allow-HTML-resource-rendering")
    public boolean getAllowHtmlResourceRendering() {
        return getBooleanValue("security.allow-HTML-resource-rendering");
    }

    public void setAllowHtmlResourceRendering(final String allowHtmlResourceRendering) {
        try {
            set(allowHtmlResourceRendering, "security.allow-HTML-resource-rendering");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'security.allow-HTML-resource-rendering': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "XNAT")
    public String getEmailPrefix() {
        return getValue("emailPrefix");
    }

    public void setEmailPrefix(final String emailPrefix) {
        try {
            set(emailPrefix, "emailPrefix");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'emailPrefix': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "org.nrg.config.services.impl.PrefsBasedSiteConfigurationService", property = "admin.siteConfig.service")
    public String getSiteConfigurationService() {
        return getValue("admin.siteConfig.service");
    }

    public void setSiteConfigurationService(final String siteConfigurationService) {
        try {
            set(siteConfigurationService, "admin.siteConfig.service");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'admin.siteConfig.service': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "5")
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

    @NrgPreference(defaultValue = "1 day")
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

    @NrgPreference(defaultValue = "31556926")
    public int getInactivityBeforeLockout() {
        return getIntegerValue("inactivityBeforeLockout");
    }

    public void setInactivityBeforeLockout(final int inactivityBeforeLockout) {
        try {
            setIntegerValue(inactivityBeforeLockout, "inactivityBeforeLockout");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'inactivityBeforeLockout': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "0 0 1 * * ?")
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

    @NrgPreference(defaultValue = "15")
    public int getSessionTimeout() {
        return getIntegerValue("sessionTimeout");
    }

    public void setSessionTimeout(final int sessionTimeout) {
        try {
            setIntegerValue(sessionTimeout, "sessionTimeout");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'sessionTimeout': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "2880")//2880 minutes = 2 days
    public long getAliasTokenTimeout() {
        return getLongValue("aliasTokenTimeout");
    }

    public void setAliasTokenTimeout(final long aliasTokenTimeout) {
        try {
            setLongValue(aliasTokenTimeout, "aliasTokenTimeout");

        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'aliasTokenTimeout': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", property = "audit.show_change_justification")
    public boolean getShowChangeJustification(){
        return getBooleanValue("audit.show_change_justification");
    }

    public void setShowChangeJustification(final boolean showChangeJustification) {
        try {
            setBooleanValue(showChangeJustification, "audit.show_change_justification");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'audit.show_change_justification': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", property = "audit.require_change_justification")
    public boolean getRequireChangeJustification(){
        return getBooleanValue("audit.require_change_justification");
    }

    public void setRequireChangeJustification(final boolean requireChangeJustification) {
        try {
            setBooleanValue(requireChangeJustification, "audit.require_change_justification");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'audit.require_change_justification': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", property = "audit.require_event_name")
    public boolean getRequireEventName(){
        return getBooleanValue("audit.require_event_name");
    }

    public void setRequireEventName(final boolean requireEventName) {
        try {
            setBooleanValue(requireEventName, "audit.require_event_name");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'audit.require_event_name': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "", property = "notifications.helpContactInfo")
    public String getHelpContactInfo(){
        return getValue("notifications.helpContactInfo");
    }

    public void setHelpContactInfo(final String helpContactInfo) {
        try {
            set(helpContactInfo, "notifications.helpContactInfo");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifications.helpContactInfo': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "", property = "notifications.emailMessageUserRegistration")
    public String getEmailMessageUserRegistration(){
        return getValue("notifications.emailMessageUserRegistration");
    }

    public void setEmailMessageUserRegistration(final String emailMessageUserRegistration) {
        try {
            set(emailMessageUserRegistration, "notifications.emailMessageUserRegistration");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifications.emailMessageUserRegistration': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "", property = "notifications.emailMessageForgotUsernameRequest")
    public String getEmailMessageForgotUsernameRequest(){
        return getValue("notifications.emailMessageForgotUsernameRequest");
    }

    public void setEmailMessageForgotUsernameRequest(final String emailMessageForgotUsernameRequest) {
        try {
            set(emailMessageForgotUsernameRequest, "notifications.emailMessageForgotUsernameRequest");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifications.emailMessageForgotUsernameRequest': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "", property = "notifications.emailMessageForgotPasswordReset")
    public String getEmailMessageForgotPasswordReset(){
        return getValue("notifications.emailMessageForgotPasswordReset");
    }

    public void setEmailMessageForgotPasswordReset(final String emailMessageForgotPasswordReset) {
        try {
            set(emailMessageForgotPasswordReset, "notifications.emailMessageForgotPasswordReset");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifications.emailMessageForgotPasswordReset': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", property = "notifications.notifyAdminUserRegistration")
    public boolean getNotifyAdminUserRegistration(){
        return getBooleanValue("notifications.notifyAdminUserRegistration");
    }

    public void setNotifyAdminUserRegistration(final boolean notifyAdminUserRegistration) {
        try {
            setBooleanValue(notifyAdminUserRegistration, "notifications.notifyAdminUserRegistration");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifications.notifyAdminUserRegistration': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", property = "notifications.notifyAdminPipelineEmails")
    public boolean getNotifyAdminPipelineEmails(){
        return getBooleanValue("notifications.notifyAdminPipelineEmails");
    }

    public void setNotifyAdminPipelineEmails(final boolean notifyAdminPipelineEmails) {
        try {
            setBooleanValue(notifyAdminPipelineEmails, "notifications.notifyAdminPipelineEmails");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifications.notifyAdminPipelineEmails': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", property = "notifications.notifyAdminProjectAccessRequest")
    public boolean getNotifyAdminProjectAccessRequest(){
        return getBooleanValue("notifications.notifyAdminProjectAccessRequest");
    }

    public void setNotifyAdminProjectAccessRequest(final boolean notifyAdminProjectAccessRequest) {
        try {
            setBooleanValue(notifyAdminProjectAccessRequest, "notifications.notifyAdminProjectAccessRequest");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifications.notifyAdminProjectAccessRequest': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false", property = "notifications.notifyAdminProjectOnSessionTransfer")
    public boolean getNotifyAdminSessionTransfer(){
        return getBooleanValue("notifications.notifyAdminProjectOnSessionTransfer");
    }

    public void setNotifyAdminSessionTransfer(final boolean notifyAdminProjectOnSessionTransfer) {
        try {
            setBooleanValue(notifyAdminProjectOnSessionTransfer, "notifications.notifyAdminProjectOnSessionTransfer");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifications.notifyAdminProjectOnSessionTransfer': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "", property = "notifications.emailRecipientErrorMessages")
    public String getEmailRecipientErrorMessages(){
        return getValue("notifications.emailRecipientErrorMessages");
    }

    public void setEmailRecipientErrorMessages(final String emailRecipientErrorMessages) {
        try {
            set(emailRecipientErrorMessages, "notifications.emailRecipientErrorMessages");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifications.emailRecipientErrorMessages': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "", property = "notifications.emailRecipientIssueReports")
    public String getEmailRecipientIssueReports(){
        return getValue("notifications.emailRecipientIssueReports");
    }

    public void setEmailRecipientIssueReports(final String emailRecipientIssueReports) {
        try {
            set(emailRecipientIssueReports, "notifications.emailRecipientIssueReports");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifications.emailRecipientIssueReports': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "", property = "notifications.emailRecipientNewUserAlert")
    public String getEmailRecipientNewUserAlert(){
        return getValue("notifications.emailRecipientNewUserAlert");
    }

    public void setEmailRecipientNewUserAlert(final String emailRecipientNewUserAlert) {
        try {
            set(emailRecipientNewUserAlert, "notifications.emailRecipientNewUserAlert");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifications.emailRecipientNewUserAlert': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "", property = "notifications.emailRecipientUpdate")
    public String getEmailRecipientUpdate(){
        return getValue("notifications.emailRecipientUpdate");
    }

    public void setEmailRecipientUpdate(final String emailRecipientUpdate) {
        try {
            set(emailRecipientUpdate, "notifications.emailRecipientUpdate");
        } catch (InvalidPreferenceName e) {
            _log.error("Invalid preference name 'notifications.emailRecipientUpdate': something is very wrong here.", e);
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
        return !StringUtils.isBlank(getSiteId()) &&
               !StringUtils.isBlank(getAdminEmail()) &&
               !StringUtils.isBlank(getArchivePath()) &&
               !StringUtils.isBlank(getPrearchivePath()) &&
               !StringUtils.isBlank(getCachePath()) &&
               !StringUtils.isBlank(getBuildPath()) &&
               !StringUtils.isBlank(getFtpPath());
    }

    private static final String STR_REQUIRE_EVENT_NAME = "audit.require_event_name";
    private static final String REQUIRE_CHANGE_JUSTIFICATION = "audit.require_change_justification";
    private static final String SHOW_CHANGE_JUSTIFICATION = "audit.show_change_justification";

    private static final Logger _log = LoggerFactory.getLogger(SiteConfigPreferences.class);
}
