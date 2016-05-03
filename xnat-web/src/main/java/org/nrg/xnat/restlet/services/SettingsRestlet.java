/*
 * org.nrg.xnat.restlet.services.SettingsRestlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/27/14 11:54 AM
 */
package org.nrg.xnat.restlet.services;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.PropertyNotFoundException;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.dicomtools.filters.*;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.utilities.Patterns;
import org.nrg.mail.api.NotificationType;
import org.nrg.notify.api.CategoryScope;
import org.nrg.notify.api.SubscriberType;
import org.nrg.notify.entities.*;
import org.nrg.notify.exceptions.DuplicateSubscriberException;
import org.nrg.notify.services.NotificationService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.ArcArchivespecificationNotificationTypeI;
import org.nrg.xdat.om.ArcArchivespecification;
import org.nrg.xdat.om.ArcArchivespecificationNotificationType;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.restlet.representations.ItemXMLRepresentation;
import org.nrg.xnat.restlet.resources.RestMockCallMapRestlet;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.security.FilterSecurityInterceptorBeanPostProcessor;
import org.nrg.xnat.security.XnatExpiredPasswordFilter;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.utils.BuildDirScriptRunnerOutputAdapter;
import org.nrg.xnat.utils.XnatHttpUtils;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;

public class SettingsRestlet extends SecureResource {

    public SettingsRestlet(Context context, Request request, Response response) throws IOException {
        super(context, request, response);

        setModifiable(true);

        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_XML));

        _filterService = XDAT.getContextService().getBean(DicomFilterService.class);

        _arcSpec = ArcSpecManager.GetInstance();
        _property = (String) getRequest().getAttributes().get("PROPERTY");

        if (!StringUtils.isBlank(_property)) {
            if (_property.equals("initialize")) {
                if (_arcSpec != null && _arcSpec.isComplete()) {
                    throw new RuntimeException("You can't initialize an already initialized system!");
                }
            } else {
                if (_arcSpec == null) {
                    throw new RuntimeException("You haven't yet initialized the system, so I can't return any values!");
                }
                _value = (String) getRequest().getAttributes().get("VALUE");
            }
        } else {
            if (_arcSpec == null) {
                throw new RuntimeException("You haven't yet initialized the system, so I can't return any values!");
            }
        }
        if (!Roles.isSiteAdmin(user)) {
            response.setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Only site admins can retrieve the system settings.");
        } else if (request.isEntityAvailable()) {
            convertFormDataToMap(request.getEntity().getText());
        }
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        final MediaType mediaType = overrideVariant(variant);

        try {
            if (StringUtils.isBlank(_property)) {
                return mediaType == MediaType.TEXT_XML ?
                        new ItemXMLRepresentation(_arcSpec.getItem(), mediaType) :
                        new StringRepresentation("{\"ResultSet\":{\"Result\":" + toJson(getArcSpecAsMap()) + ", \"title\": \"Settings\"}}");
            } else {
                if (!getArcSpecAsMap().containsKey(_property)) {
                    throw new PropertyNotFoundException(String.format("Setting '%s' was not found in the system.", _property));
                }
                Object propertyValue = getArcSpecAsMap().get(_property);

                if (mediaType == MediaType.TEXT_XML) {
                    String xml = "<" + _property + ">" + propertyValue.toString() + "</" + _property + ">";
                    return new StringRepresentation(xml, mediaType);
                } else {
                    return new StringRepresentation("{\"ResultSet\":{\"Result\":" + toJson(propertyValue) + ", \"title\": \"" + _property + "\"}}");
                }
            }
        } catch (PropertyNotFoundException exception) {
            respondToException(exception, Status.CLIENT_ERROR_BAD_REQUEST);
        } catch (IOException | ConfigServiceException exception) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Something went wrong converting filters to JSON.", exception);
        }
        return null;
    }

    private Map<Object, Object> getArcSpecAsMap() throws IOException, ConfigServiceException {
        Map<Object, Object> settings = new HashMap<>();

        final SiteConfigPreferences preferences = XDAT.getContextService().getBean(SiteConfigPreferences.class);

        settings.putAll(XDAT.getSiteConfiguration());
        settings.put("siteId", preferences.getSiteId());
        final String siteUrl = StringUtils.isBlank(_arcSpec.getSiteUrl()) ? XnatHttpUtils.getServerRoot(getHttpServletRequest()) : _arcSpec.getSiteUrl();
        settings.put("siteUrl", siteUrl);
        settings.put("siteAdminEmail", _arcSpec.getSiteAdminEmail());
        settings.put("smtpHost", _arcSpec.getSmtpHost());
        settings.put("requireLogin", _arcSpec.getRequireLogin());
        settings.put("enableNewRegistrations", _arcSpec.getEnableNewRegistrations());
        settings.put("emailVerification", XDAT.getSiteConfigurationProperty("emailVerification"));
        settings.put("archivePath", _arcSpec.getGlobalArchivePath());
        settings.put("prearchivePath", _arcSpec.getGlobalPrearchivePath());
        settings.put("cachePath", _arcSpec.getGlobalCachePath());
        settings.put("buildPath", _arcSpec.getGlobalBuildPath());
        settings.put("ftpPath", _arcSpec.getGlobalpaths().getFtppath());
        settings.put("pipelinePath", _arcSpec.getGlobalpaths().getPipelinepath());
        // MIGRATION: This needs to go away when there's a proper DICOM SCP edit page.
        settings.put("dcmPort", 8104);
        settings.put("dcmAe", "XNAT");
        settings.put("enableCsrfToken", _arcSpec.getEnableCsrfToken());
        settings.put("error", getSubscribersForEvent(NotificationType.Error));
        settings.put("issue", getSubscribersForEvent(NotificationType.Issue));
        settings.put("newUser", getSubscribersForEvent(NotificationType.NewUser));
        settings.put("update", getSubscribersForEvent(NotificationType.Update));
        settings.put("anonScript", emptyStringIfNull(XDAT.getConfigService().getConfigContents("anon", "script")));
        settings.put("anonEnabled", Configuration.ENABLED_STRING.equals(XDAT.getConfigService().getStatus("anon", "script")));
        settings.put("tracerList", emptyStringIfNull(XDAT.getConfigService().getConfigContents("tracers", "tracers")));
        settings.put("appletScript", emptyStringIfNull(XDAT.getConfigService().getConfigContents("applet", "settings")));
        settings.put("restMockCallMap", getFormattedRestMockCallMap());
        final Map<String, String> filterAsMap = getSeriesImportFilterAsMap();
        if (filterAsMap != null && filterAsMap.size() > 0) {
            settings.putAll(filterAsMap);
        }

        return settings;
    }

    private String emptyStringIfNull(final String configContents) {
        return configContents == null ? "" : configContents;
    }

    private DicomFilterService getDicomFilterService() {
        return _filterService;
    }

    private SeriesImportFilter getSeriesImportFilter() {
        if (_seriesImportFilter == null) {
            _seriesImportFilter = getDicomFilterService().getSeriesImportFilter();
        }
        return _seriesImportFilter;
    }

    private Map<String, String> getSeriesImportFilterAsMap() {
        final SeriesImportFilter filter = getSeriesImportFilter();
        return filter == null ? null : filter.toQualifiedMap();
    }

    private String getFormattedRestMockCallMap() {
        Map<String, String> map = RestMockCallMapRestlet.getRestMockCallMap();
        if (map == null || map.size() == 0) {
            return StringUtils.EMPTY;
        }
        StringBuilder formattedMap = new StringBuilder();
        for (Map.Entry<String, String> call : map.entrySet()) {
            formattedMap.append(call.getKey()).append("|").append(call.getValue()).append("\n");
        }
        return formattedMap.toString();
    }

    private String getUnformattedRestMockCallMap(String raw) {
        if (StringUtils.isBlank(raw)) {
            return StringUtils.EMPTY;
        }
        BufferedReader reader = new BufferedReader(new StringReader(raw));
        Map<String, String> map = new HashMap<>();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                String[] atoms = line.split("\\|");
                map.put(atoms[0], atoms[1]);
            }

            return toJson(map);
        } catch (IOException ignored) {
            // We're not reading from a file, so we shouldn't encounter this.
        }

        return StringUtils.EMPTY;
    }

    // TODO: Gross.
    private static final String ADMIN_USERNAME_FOR_SUBSCRIPTION = "admin";

    /**
     * This returns the current subscriber or subscribers to a particular <i>site-wide</i> event. If the event doesn't
     * already exist, the event will be created with the default user set to the site administrator's email address.
     *
     * @param event The event to be created or retrieved.
     */
    private synchronized String getSubscribersForEvent(NotificationType event) {
        Category category;
        Definition definition;

        synchronized (_log) {
            category = getSiteEventCategory(event);
            definition = getSiteEventDefinition(category);
        }

        Map<Subscriber, Subscription> subscriptions = getNotificationService().getSubscriptionService().getSubscriberMapOfSubscriptionsForDefinition(definition);
        if (subscriptions != null && subscriptions.size() > 0) {
            return createCommaSeparatedList(subscriptions.keySet());
        } else {
            Subscriber adminUser = getNotificationService().getSubscriberService().getSubscriberByName(ADMIN_USERNAME_FOR_SUBSCRIPTION);
            if (adminUser == null) {
                try {
                    adminUser = getNotificationService().getSubscriberService().createSubscriber(ADMIN_USERNAME_FOR_SUBSCRIPTION, _arcSpec.getSiteAdminEmail());
                } catch (DuplicateSubscriberException exception) {
                    // This shouldn't happen, since we just checked for the subscriber's existence.
                }
            }

            getNotificationService().subscribe(adminUser, SubscriberType.User, definition, XDAT.getHtmlMailChannel());
            assert adminUser != null;
            return adminUser.getEmails();
        }
    }

    private synchronized Category getSiteEventCategory(NotificationType event) {
        Category category = getNotificationService().getCategoryService().getCategoryByScopeAndEvent(CategoryScope.Site, event.toString());
        if (category == null) {
            category = initializeSiteEventCategory(event);
        }
        return category;
    }

    private synchronized Definition getSiteEventDefinition(Category category) {
        Definition definition;
        List<Definition> definitions = getNotificationService().getDefinitionService().getDefinitionsForCategory(category);
        if (definitions == null || definitions.size() == 0) {
            definition = initializeSiteEventDefinition(category);
        } else {
            definition = definitions.get(0);
        }
        return definition;
    }

    private String createCommaSeparatedList(final Set<Subscriber> subscribers) {
        if (subscribers == null || subscribers.size() == 0) {
            return "";
        }
        boolean isFirst = true;
        StringBuilder buffer = new StringBuilder();
        for (Subscriber subscriber : subscribers) {
            if (isFirst) {
                isFirst = false;
            } else {
                buffer.append(", ");
            }
            buffer.append(subscriber.getEmails());
        }
        return buffer.toString();
    }

    @Override
    public void handlePost() {
        setProperties();
    }

    @Override
    public void handlePut() {
        setProperties();
    }

    @Override
    public void handleDelete() {
        _log.warn("Got a request to delete property with ID: " + _property + " from user: " + user.getLogin());
        returnDefaultRepresentation();
    }

    private void setProperties() {
        if (!Roles.isSiteAdmin(user)) {
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "You must be an administrator to modify the site settings.");
            return;
        }
        try {
            if (!StringUtils.isBlank(_property) && !_property.equals("initialize")) {
                setDiscreteProperty();
                checkNotifications();
                returnDefaultRepresentation();
            } else if (!StringUtils.isBlank(_property)) {
                // We will only enter this if _property is "initialize", so that means we need to set up the arc spec entry.
                initializeArcSpec();
                checkNotifications();
                setPropertiesFromMap(_data);
                // Do not return a representation if we are initializing, it will attempt (and fail) to find a property called "initialize"
            } else {
                setPropertiesFromMap(_data);
                returnDefaultRepresentation();
            }
        } catch (ConfigServiceException exception) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Something went wrong retrieving site properties.", exception);
        } catch (Exception exception) {
            respondToException(exception, Status.CLIENT_ERROR_BAD_REQUEST);
        }
    }

    private void setPropertiesFromMap(Map<String, String> map) throws Exception {
        _log.debug("Setting arc spec property from body string: " + _form);
        boolean dirtied = false;
        boolean dirtiedNotifications = false;
        final Map<String, String> notifications = new HashMap<>();
        final Map<String, String> filter = new HashMap<>();
        for (String property : map.keySet()) {
            if (property.equals("siteId")) {
                final String siteId = map.get("siteId");
                _arcSpec.setSiteId(siteId);
                XFT.SetSiteID(siteId);
                dirtied = true;
            } else if (property.equals("siteUrl")) {
                final String siteUrl = map.get("siteUrl");
                _arcSpec.setSiteUrl(siteUrl);
                XFT.SetSiteURL(siteUrl);
                dirtied = true;
            } else if (property.equals("siteAdminEmail")) {
                final String siteAdminEmail = map.get("siteAdminEmail");
                _arcSpec.setSiteAdminEmail(siteAdminEmail);
                XFT.SetAdminEmail(siteAdminEmail);
                dirtied = true;
            } else if (property.equals("smtpHost")) {
                final String smtpHost = map.get("smtpHost");
                _arcSpec.setSmtpHost(smtpHost);
                XFT.SetAdminEmailHost(smtpHost);
                dirtied = true;
            } else if (property.equals("requireLogin")) {
                final String requireLogin = map.get("requireLogin");
                _arcSpec.setRequireLogin(requireLogin);
                XFT.SetRequireLogin(requireLogin);
                updateSecurityFilter(Boolean.parseBoolean(requireLogin));
                dirtied = true;
            } else if (property.equals("enableNewRegistrations")) {
                final String enableNewRegistrations = map.get("enableNewRegistrations");
                _arcSpec.setEnableNewRegistrations(enableNewRegistrations);
                XFT.SetUserRegistration(enableNewRegistrations);
                dirtied = true;
            } else if (property.equals("archivePath")) {
                final String archivePath = map.get("archivePath");
                _arcSpec.getGlobalpaths().setArchivepath(archivePath);
                XFT.SetArchiveRootPath(archivePath);
                dirtied = true;
            } else if (property.equals("prearchivePath")) {
                final String prearchivePath = map.get("prearchivePath");
                _arcSpec.getGlobalpaths().setPrearchivepath(prearchivePath);
                XFT.SetPrearchivePath(prearchivePath);
                dirtied = true;
            } else if (property.equals("cachePath")) {
                final String cachePath = map.get("cachePath");
                _arcSpec.getGlobalpaths().setCachepath(cachePath);
                XFT.SetCachePath(cachePath);
                dirtied = true;
            } else if (property.equals("buildPath")) {
                final String buildPath = map.get("buildPath");
                _arcSpec.getGlobalpaths().setBuildpath(buildPath);
                XFT.setBuildPath(buildPath);
                dirtied = true;
            } else if (property.equals("ftpPath")) {
                final String ftpPath = map.get("ftpPath");
                _arcSpec.getGlobalpaths().setFtppath(ftpPath);
                XFT.setFtpPath(ftpPath);
                dirtied = true;
            } else if (property.equals("pipelinePath")) {
                final String pipelinePath = map.get("pipelinePath");
                _arcSpec.getGlobalpaths().setPipelinepath(pipelinePath);
                XFT.SetPipelinePath(pipelinePath);
                dirtied = true;
            } else if (property.equals("dcmPort")) {
                _arcSpec.setDcm_dcmPort(map.get("dcmPort"));
                dirtied = true;
            } else if (property.equals("dcmAe")) {
                _arcSpec.setDcm_dcmAe(map.get("dcmAe"));
                dirtied = true;
            } else if (property.equals("enableCsrfToken")) {
                final String enableCsrfToken = map.get("enableCsrfToken");
                _arcSpec.setEnableCsrfToken(enableCsrfToken);
                XFT.SetEnableCsrfToken(enableCsrfToken);
                dirtied = true;
            } else if (property.equals("error") || property.equals("issue") || property.equals("newUser") || property.equals("update")) {
                if (!dirtiedNotifications) {
                    dirtiedNotifications = true;
                    clearArcSpecNotifications();
                }
                notifications.put(property, map.get(property));
            } else if (property.equals("anonScript")) {
                final String anonScript = map.get("anonScript");
                try {
                    XDAT.getConfigService().replaceConfig(user.getUsername(), "Updating the site-wide anonymization script", "anon", "script", anonScript);
                } catch (ConfigServiceException exception) {
                    throw new Exception("Error setting the site-wide anonymization script", exception);
                }
                dirtied = true;
            } else if (property.equals("tracerList")) {
                final String tracerList = map.get("tracerList");
                try {
                    XDAT.getConfigService().replaceConfig(user.getUsername(), "Updating the site-wide list of PET tracers", "tracers", "tracers", tracerList);
                } catch (ConfigServiceException exception) {
                    throw new Exception("Error setting the site-wide list of PET tracers", exception);
                }
                dirtied = true;
            } else if (property.equals("appletScript")) {
                final String appletScript = map.get("appletScript");
                try {
                    XDAT.getConfigService().replaceConfig(user.getUsername(), "Updating the site-wide applet settings", "applet", "settings", appletScript);
                } catch (ConfigServiceException exception) {
                    throw new Exception("Error setting the site-wide applet settings", exception);
                }
                dirtied = true;
            } else if (property.equals("restMockCallMap")) {
                final String callMap = map.get("restMockCallMap");
                try {
                    XDAT.getConfigService().replaceConfig(user.getUsername(), "Updating the REST service mock call map", "rest", "mockCallMap", getUnformattedRestMockCallMap(callMap));
                } catch (ConfigServiceException exception) {
                    throw new Exception("Error setting the REST service mock call map", exception);
                }
                dirtied = true;
            } else if (property.equals("enableProjectAppletScript")) {
                final String enableProjectAppletScript = map.get("enableProjectAppletScript");
                try {
                    XDAT.setSiteConfigurationProperty("enableProjectAppletScript", enableProjectAppletScript);
                } catch (ConfigServiceException exception) {
                    throw new Exception("Error setting the enableProjectAppletScript site info property", exception);
                }
                dirtied = true;
            } else if (property.equals("emailVerification")) {
                final String emailVerification = map.get("emailVerification");
                try {
                    XDAT.setSiteConfigurationProperty("emailVerification", emailVerification);
                } catch (ConfigServiceException exception) {
                    throw new Exception("Error setting the emailVerification site info property", exception);
                }
                dirtied = true;
            } else if (property.startsWith("passwordExpiration")) {
                try {
                    final String current = XDAT.getSiteConfigurationProperty(property);
                    final String value = map.get(property);
                    if (!StringUtils.equals(current, value)) {
                        dirtied = true;
                        XDAT.getContextService().getBean(XnatExpiredPasswordFilter.class).setPasswordExpirationDirtied(true);
                        XDAT.setSiteConfigurationProperty(property, value);
                    }
                } catch (ConfigServiceException exception) {
                    throw new Exception(String.format("Error getting the '%s' site info property", property), exception);
                }
            } else if (property.startsWith("seriesImportFilter")) {
                switch (property) {
                    case "seriesImportFilterList":
                        filter.put("contents", map.get(property).trim());
                        break;
                    case "seriesImportFilterMode":
                        filter.put("mode", map.get(property).trim());
                        break;
                    case "seriesImportFilterEnabled":
                        filter.put("enabled", map.get(property).trim());
                        break;
                }
            } else {
                try {
                    XDAT.setSiteConfigurationProperty(property, map.get(property));
                } catch (ConfigServiceException exception) {
                    throw new Exception(String.format("Error setting the '%s' site info property", property), exception);
                }
                dirtied = true;
            }
        }
        if (filter.size() > 0) {
            final boolean enabled = Boolean.parseBoolean(filter.get("enabled"));
            final SeriesImportFilterMode mode = SeriesImportFilterMode.mode(filter.get("mode"));
            final SeriesImportFilter seriesImportFilter;
            if (mode == SeriesImportFilterMode.ModalityMap) {
                seriesImportFilter = new ModalityMapSeriesImportFilter(filter.get("contents"), enabled);
            } else {
                seriesImportFilter = new RegExBasedSeriesImportFilter(filter.get("contents"), mode, enabled);
            }
            if (!seriesImportFilter.equals(getSeriesImportFilter())) {
                _seriesImportFilter = null;
                getDicomFilterService().commit(seriesImportFilter, userName, "Updated site-wide series import filter from administrator UI.");
            }
        }
        if (dirtied || dirtiedNotifications) {
            if (notifications.size() > 0) {
                boolean allowNonuserSubscribers = XDAT.getBoolSiteConfigurationProperty("emailAllowNonuserSubscribers", false);
                for (Map.Entry<String, String> notification : notifications.entrySet()) {
                    configureEventSubscriptions(NotificationType.valueOf(StringUtils.capitalize(notification.getKey())), notification.getValue(), allowNonuserSubscribers);
                }
            }
            ArcSpecManager.save(_arcSpec, user, newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, "Modified archive specification"));
        }
        if (map.containsKey("anonEnabled")) {
            boolean anonymize = map.get("anonEnabled").equals("true");
            boolean anonScriptCurrentlyEnabled = XDAT.getConfigService().getConfig("anon", "script").isEnabled();
            if (anonymize && !anonScriptCurrentlyEnabled) {
                try {
                    XDAT.getConfigService().enable(user.getUsername(), "Enabling the site-wide anonymization script", "anon", "script");
                } catch (ConfigServiceException e) {
                    e.printStackTrace();
                }
            }
            if (!anonymize && anonScriptCurrentlyEnabled) {
                try {
                    XDAT.getConfigService().disable(user.getUsername(), "Disabling the site-wide anonymization script", "anon", "script");
                } catch (ConfigServiceException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateSecurityFilter(final boolean requireLogin) {
        FilterSecurityInterceptor interceptor = XDAT.getContextService().getBean(FilterSecurityInterceptor.class);
        FilterSecurityInterceptorBeanPostProcessor postProcessor = XDAT.getContextService().getBean(FilterSecurityInterceptorBeanPostProcessor.class);
        assert interceptor != null;
        assert postProcessor != null;

        interceptor.setSecurityMetadataSource(postProcessor.getMetadataSource(requireLogin));
    }

    /**
     * Sets up the subscriptions for the indicated events.
     *
     * @param notificationType        The type of notification for which to configure subscriptions.
     * @param userIds                 The IDs of the users to be subscribed to the indicated notification.
     * @param allowNonuserSubscribers Indicates whether email addresses that are not associated with system users
     *                                should be allowed for subscription addresses.
     */
    private void configureEventSubscriptions(final NotificationType notificationType, final String userIds, final boolean allowNonuserSubscribers) throws Exception {
        Definition definition = retrieveSiteEventDefinition(notificationType);
        List<Subscriber> subscribers = getSubscribersFromAddresses(userIds, allowNonuserSubscribers);
        Map<Subscriber, Subscription> subscriptions = getNotificationService().getSubscriptionService().getSubscriberMapOfSubscriptionsForDefinition(definition);
        Channel channel = XDAT.getHtmlMailChannel();

        for (Subscriber subscriber : subscribers) {
            // If we don't have a subscription for this notification...
            if (!subscriptions.containsKey(subscriber)) {
                // Create one.
                getNotificationService().subscribe(subscriber, SubscriberType.User, definition, channel);
                // But if we do have a subscription for this notification...
            } else {
                // Remove it from the map.
                subscriptions.remove(subscriber);
            }
        }

        // If there are any left-over subscriptions...
        if (subscriptions.size() > 0) {
            // Those are no longer wanted (they weren't specified in the submitted list), so let's remove those subscriptions.
            for (Subscription subscription : subscriptions.values()) {
                getNotificationService().getSubscriptionService().delete(subscription);
            }
        }

        configureArcSpecNotificationType(_arcSpec, notificationType, subscriptions.values());
    }

    /**
     * @param type The type for which the definition and its associated category should be created or retrieved.
     * @return The existing or newly created definition.
     */
    private synchronized Definition retrieveSiteEventDefinition(NotificationType type) {
        Category category = getNotificationService().getCategoryService().getCategoryByScopeAndEvent(CategoryScope.Site, type.toString());
        if (category == null) {
            category = initializeSiteEventCategory(type.toString());
        }
        List<Definition> definitions = getNotificationService().getDefinitionService().getDefinitionsForCategory(category);
        Definition definition;
        if (definitions == null || definitions.size() == 0) {
            definition = initializeSiteEventDefinition(category);
        } else {
            definition = definitions.get(0);
        }
        return definition;
    }

    /**
     * @param event The event for which a category should be created.
     * @return The newly created category object.
     */
    private synchronized Category initializeSiteEventCategory(NotificationType event) {
        return initializeSiteEventCategory(event.toString());
    }

    /**
     * @param event The event for which a category should be created.
     * @return The newly created category object.
     */
    private synchronized Category initializeSiteEventCategory(String event) {
        Category category = getNotificationService().getCategoryService().newEntity();
        category.setScope(CategoryScope.Site);
        category.setEvent(event);
        getNotificationService().getCategoryService().create(category);
        return category;
    }

    /**
     * @param category The category for which a definition should be created.
     * @return The newly created definition object.
     */
    private synchronized Definition initializeSiteEventDefinition(Category category) {
        Definition definition = getNotificationService().getDefinitionService().newEntity();
        definition.setCategory(category);
        getNotificationService().getDefinitionService().create(definition);
        return definition;
    }

    /**
     * Takes a comma-separated list of "email addresses" (which actually may include {@link UserI#getEmail() email addresses},
     * {@link UserI#getLogin() XDAT user names}, and a combination of the two in the format:
     * <p/>
     * <code><i>username</i> &lt;<i>email</i>&gt;</code>
     * <p/>
     * So for example, you may have something like this:
     * <p/>
     * <code>user1 &lt;user1@@xnat.org&gt;, user2, user3@xnat.org</code>
     * <p/>
     * Note that if any of the users aren't found, this method currently will have no indication other than returning fewer
     * users than are specified in the <b>emailAddresses</b> parameter.
     *
     * @param addressList             The comma-separated list of usernames, email addressList, and combined IDs.
     * @param allowNonuserSubscribers Indicates whether email addresses that are not associated with system users
     *                                should be allowed for subscription addresses.
     * @return A list of {@link Subscriber} objects representing those users.
     */
    private List<Subscriber> getSubscribersFromAddresses(String addressList, final boolean allowNonuserSubscribers) throws Exception {
        final String[] addresses = addressList.split("[\\s]*,[\\s]*");
        if (addresses.length == 0) {
            throw new Exception("Submitted text couldn't be parsed into a list of addresses: " + addressList);
        }

        List<Subscriber> subscribers = new ArrayList<>();
        List<String> nonuserAddresses = new ArrayList<>();
        for (String address : addresses) {
            String username = null;
            String email = null;
            if (Patterns.USERNAME.matcher(address).matches()) {
                // Handle this as a username.
                UserI user = null;
                try {
                    user = Users.getUser(address);
                } catch (Exception ignored) {
                }
                if (user != null) {
                    username = address;
                    email = user.getEmail();
                }
            } else if (Patterns.EMAIL.matcher(address).matches()) {
                // Handle this as an email.
                List<UserI> users = Users.getUsersByEmail(address);
                if (users != null && users.size() > 0) {
                    if (users.size() == 1) {
                        username = users.get(0).getLogin();
                    } else {
                        for (UserI user : users) {
                            username = user.getLogin();
                            if (Roles.isSiteAdmin(user)) {
                                break;
                            }
                        }
                    }
                    email = address;
                } else if (allowNonuserSubscribers) {
                    // If we allow non-user subscribers, then we'll just make the username and email address be the same.
                    username = email = address;
                }
            } else {
                Matcher combinedMatcher = Patterns.COMBINED.matcher(address);
                if (combinedMatcher.matches()) {
                    // Handle this as a combined. username will match first capture, email second capture (0 capture in regex is full expression).
                    username = combinedMatcher.group(1);
                    email = combinedMatcher.group(2);
                }
            }

            // If there's no username, this is a non-user address on a system that doesn't allow non-user subscribers;
            // continue so we can harvest ALL the non-user addresses, then handle them appropriately later.
            if (username == null) {
                nonuserAddresses.add(address);
                continue;
            }

            // If we don't have any non-user addresses, get the subscriber. If we do have any non-user addresses, we
            // have a valid user but we'll skip getting the subscriber to save ourselves the work.
            if (nonuserAddresses.size() == 0) {
                Subscriber subscriber = getNotificationService().getSubscriberService().getSubscriberByName(username);
                if (subscriber == null) {
                    try {
                        subscriber = getNotificationService().getSubscriberService().createSubscriber(username, email);
                    } catch (DuplicateSubscriberException exception) {
                        // This shouldn't happen, since we just checked for the subscriber's existence.
                    }
                }

                assert subscriber != null;
                if (!subscriber.getEmails().equalsIgnoreCase(email)) {
                    subscriber.setEmails(email);
                    getNotificationService().getSubscriberService().update(subscriber);
                }
                subscribers.add(subscriber);
            }
        }

        if (nonuserAddresses.size() > 0) {
            throw new Exception(getBadAddressErrorMessage(nonuserAddresses));
        }

        return subscribers;
    }

    private String getBadAddressErrorMessage(final List<String> badAddresses) {
        StringBuilder buffer = new StringBuilder("<p>The following addresses were not valid usernames or emails of users of this XNAT server:</p><ul>");
        for (String address : badAddresses) {
            buffer.append("<li>").append(address).append("</li>");
        }
        buffer.append("</ul>");
        return buffer.toString();
    }

    private void initializeArcSpec() throws Exception {
        PopulateItem populator = new PopulateItem(copyDataToXmlPath(), user, "arc:ArchiveSpecification", true);
        XFTItem item = populator.getItem();
        item.setUser(user);
        ArcSpecManager.save(new ArcArchivespecification(item), user, newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, "Initialized archive specification"));
        _arcSpec = ArcSpecManager.GetInstance();
        BuildDirScriptRunnerOutputAdapter adapter = XDAT.getContextService().getBean(BuildDirScriptRunnerOutputAdapter.class);
        adapter.setPath(_arcSpec.getGlobalBuildPath());
    }

    private void setDiscreteProperty() throws Exception {
        if (getRequest().getMethod() != Method.POST) {
            if (_log.isDebugEnabled()) {
                _log.debug("Setting arc spec property: [" + _property + "] = " + _value);
            }
            if (!getArcSpecAsMap().containsKey(_property)) {
                throw new PropertyNotFoundException(String.format("Setting '%s' was not found in the system.", _property));
            }
        } else if (_log.isInfoEnabled()) {
            _log.info("Requested POST to property: [" + _property + "] = " + _value);
        }
        if (null == _value) {
            throw new NullPointerException(String.format("Setting '%s' cannot be set to NULL.", _property));
        } else {
            Map<String, String> map = new HashMap<>(1);
            map.put(_property, _value);
            setPropertiesFromMap(map);
        }
    }

    private Map<String, String> copyDataToXmlPath() {
        Map<String, String> data = new HashMap<>(_data.size());
        addSpecifiedProperty(data, "arc:archivespecification/site_id", "siteId");
        addSpecifiedProperty(data, "arc:archivespecification/site_url", "siteUrl");
        addSpecifiedProperty(data, "arc:archivespecification/site_admin_email", "siteAdminEmail");
        addSpecifiedProperty(data, "arc:archivespecification/smtp_host", "smtpHost");
        addSpecifiedProperty(data, "arc:archivespecification/globalpaths/archivepath", "archivePath");
        addSpecifiedProperty(data, "arc:archivespecification/globalpaths/prearchivepath", "prearchivePath");
        addSpecifiedProperty(data, "arc:archivespecification/globalpaths/cachepath", "cachePath");
        addSpecifiedProperty(data, "arc:archivespecification/globalpaths/ftppath", "ftpPath");
        addSpecifiedProperty(data, "arc:archivespecification/globalpaths/buildpath", "buildPath");
        addSpecifiedProperty(data, "arc:archivespecification/globalpaths/pipelinepath", "pipelinePath");
        addSpecifiedProperty(data, "arc:archivespecification/require_login", "requireLogin");
        addSpecifiedProperty(data, "arc:archivespecification/enable_new_registrations", "enableNewRegistrations");
        addSpecifiedProperty(data, "arc:archivespecification/dcm/dcm_ae", "dcmAe");
        addSpecifiedProperty(data, "arc:archivespecification/dcm/dcm_port", "dcmPort");
        addSpecifiedProperty(data, "arc:archivespecification/enable_csrf_token", "enableCsrfToken");
        return data;
    }

    /**
     * Checks whether the {@link #_data} map contains the specified key and whether the corresponding value is blank.
     * If the key exists and the value is not blank, this method puts the value into the submitted data map using the
     * submitted <b>xmlPath</b> as the key.
     *
     * @param data    The data map to be populated with existing non-blank values.
     * @param xmlPath The key to use for storage in the data map.
     * @param key     The key to check in the parsed data map.
     */
    private void addSpecifiedProperty(final Map<String, String> data, final String xmlPath, final String key) {
        if (_data.containsKey(key)) {
            String value = _data.get(key);
            if (!StringUtils.isBlank(value)) {
                data.put(xmlPath, value);
            }
        }
    }

    private void convertFormDataToMap(String text) {
        _form = text;
        _data = new HashMap<>();
        String[] entries = text.split("&");
        for (String entry : entries) {
            String[] atoms = entry.split("=", 2);
            if (atoms.length > 0) {
                if (atoms.length == 1) {
                    _data.put(atoms[0], "");
                } else {
                    try {
                        _data.put(atoms[0], URLDecoder.decode(atoms[1], "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        // This is the dumbest exception in the history of humanity: the form of this method that doesn't
                        // specify an encoding is deprecated, so you have to specify an encoding. But the form of the method
                        // that takes an encoding (http://bit.ly/yX56fe) has an note that emphasizes that you should only
                        // use UTF-8 because "[n]ot doing so may introduce incompatibilities." Got it? You have to specify
                        // it, but it should always be the same thing. Oh, and BTW? You have to catch an exception for
                        // unsupported encodings because you may specify that one acceptable encoding or... something.
                        //
                        // I hate them.
                    }
                }
            }
        }
    }

    /**
     * Checks whether site-side notifications exist and initializes them if not.
     *
     * @throws Exception
     */
    private void checkNotifications() throws Exception {
        // Check whether any notification types already exist and clear them if so.
        clearArcSpecNotifications();

        for (NotificationType type : NotificationType.values()) {
            Definition definition = retrieveSiteEventDefinition(type);
            List<Subscription> subscriptions = getNotificationService().getSubscriptionService().getSubscriptionsForDefinition(definition);
            configureArcSpecNotificationType(_arcSpec, type, subscriptions);
        }
    }

    private void configureArcSpecNotificationType(final ArcArchivespecification arcSpec, final NotificationType type, final Collection<Subscription> subscriptions) throws Exception {
        ArcArchivespecificationNotificationTypeI typeObj = new ArcArchivespecificationNotificationType();
        typeObj.setNotificationType(type.id());
        if (subscriptions == null || subscriptions.size() == 0) {
            typeObj.setEmailAddresses(getSiteAdminAccount(arcSpec) + " <" + arcSpec.getSiteAdminEmail() + ">");
        } else {
            StringBuilder buffer = new StringBuilder();
            boolean isFirst = true;
            for (Subscription subscription : subscriptions) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    buffer.append(", ");
                }
                buffer.append(subscription.getSubscriber());
            }
            typeObj.setEmailAddresses(buffer.toString());
        }
        arcSpec.addNotificationTypes_notificationType(typeObj);
    }

    /**
     * Clears the arc spec notification listings.
     */
    private void clearArcSpecNotifications() {
        List<ArcArchivespecificationNotificationTypeI> notificationTypes;
        while ((notificationTypes = _arcSpec.getNotificationTypes_notificationType()) != null && notificationTypes.size() > 0) {
            _arcSpec.removeNotificationTypes_notificationType(0);
        }
    }

    /**
     * Gets the notification service instance.
     *
     * @return The notification service instance.
     */
    private NotificationService getNotificationService() {
        if (_notificationService == null) {
            _notificationService = XDAT.getNotificationService();
        }
        return _notificationService;
    }

    /**
     * Gets the site admin account name.
     *
     * @param arcSpec The arc spec from which the admin account can be retrieved.
     * @return The site admin account name.
     */
    private String getSiteAdminAccount(ArcArchivespecification arcSpec) {
        List<UserI> users = Users.getUsersByEmail(arcSpec.getSiteAdminEmail());
        if (users == null || users.size() == 0) {
            throw new RuntimeException("Can't find anything for the site admin email account! It must be associated with a user account: " + arcSpec.getSiteAdminEmail());
        }
        return users.get(0).getLogin();
    }

    private static final Logger _log = LoggerFactory.getLogger(SettingsRestlet.class);

    private final DicomFilterService _filterService;

    private NotificationService _notificationService;
    private ArcArchivespecification _arcSpec;
    private SeriesImportFilter _seriesImportFilter;
    private String _property;
    private String _value;
    private String _form;
    private Map<String, String> _data;
}
