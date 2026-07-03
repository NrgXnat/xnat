/*
 * web: org.nrg.xapi.rest.notifications.NotificationsApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.notifications;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.services.SerializerService;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.nrg.xdat.preferences.SmtpServer;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.utils.XnatHttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.nrg.xdat.preferences.SiteConfigPreferences.SITE_URL;
import static org.nrg.xdat.security.helpers.AccessLevel.Admin;

@Tag(name = "notifications", description = "XNAT Notifications management API")
@XapiRestController
@RequestMapping(value = "/notifications")
@Slf4j
public class NotificationsApi extends AbstractXapiRestController {
    public static final String POST_PROPERTIES_NOTES = "Sets the mail service host, port, username, password, and protocol. You can set "
                                                       + "extra properties on the mail sender (e.g. for configuring SSL or TLS transport) by "
                                                       + "specifying the property name and value. Any parameters submitted that are not one "
                                                       + "of the standard mail sender attributes is set as a mail sender property. You can "
                                                       + "remove existing properties by setting the property with an empty value. This will "
                                                       + "override any existing configuration. You can change the values of properties by calling "
                                                       + "the API method for that specific property or by calling the PUT version of this method.";
    public static final String PUT_PROPERTIES_NOTES  = "Sets the mail service host, port, username, password, and protocol. You can set "
                                                       + "extra properties on the mail sender (e.g. for configuring SSL or TLS transport) by "
                                                       + "specifying the property name and value. Any parameters submitted that are not one "
                                                       + "of the standard mail sender attributes is set as a mail sender property. You can "
                                                       + "remove existing properties by setting the property with an empty value. This will "
                                                       + "modify the existing server configuration. You can completely replace the configuration "
                                                       + "by calling the POST version of this method.";

    @Autowired
    public NotificationsApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final NotificationsPreferences notificationsPrefs, final XnatAppInfo appInfo, final SerializerService serializer) {
        super(userManagementService, roleHolder);
        _notificationsPrefs = notificationsPrefs;
        _appInfo = appInfo;
        _serializer = serializer;
    }

    @Operation(summary = "Returns the full map of site configuration properties.", description = "Complex objects may be returned as encapsulated JSON strings.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Site configuration properties successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set site configuration properties."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<Properties> getNotificationsProperties(@Parameter(hidden = true) final HttpServletRequest request) {
        final String username = getSessionUser().getUsername();
        log.debug("User {} requested the site configuration.", username);

        final Properties preferences = _notificationsPrefs.asProperties();

        if (!_appInfo.isInitialized()) {
            log.info("The site is being initialized by user {}. Setting default values from context.", username);
            preferences.put(SITE_URL, XnatHttpUtils.getServerRoot(request));
        }

        preferences.put("emailRecipientErrorMessages", _notificationsPrefs.getEmailRecipientErrorMessages());
        preferences.put("emailRecipientIssueReports", _notificationsPrefs.getEmailRecipientIssueReports());
        preferences.put("emailRecipientNewUserAlert", _notificationsPrefs.getEmailRecipientNewUserAlert());
        preferences.put("emailRecipientUpdate", _notificationsPrefs.getEmailRecipientUpdate());

        return new ResponseEntity<>(preferences, HttpStatus.OK);
    }

    @Operation(summary = "Sets a map of notifications properties.", description = "Sets the notifications properties specified in the map.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Notifications properties successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set notifications properties."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Void> setNotificationsProperties(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The map of notifications properties to be set.", required = true) @RequestBody final Properties properties) {
        logSetProperties(properties);

        for (final String name : properties.stringPropertyNames()) {
            try {
                if (StringUtils.equals(name, "emailRecipientErrorMessages")) {
                    _notificationsPrefs.setEmailRecipientErrorMessages(properties.getProperty(name));
                } else if (StringUtils.equals(name, "emailRecipientIssueReports")) {
                    _notificationsPrefs.setEmailRecipientIssueReports(properties.getProperty(name));
                } else if (StringUtils.equals(name, "emailRecipientNewUserAlert")) {
                    _notificationsPrefs.setEmailRecipientNewUserAlert(properties.getProperty(name));
                } else if (StringUtils.equals(name, "emailRecipientUpdate")) {
                    _notificationsPrefs.setEmailRecipientUpdate(properties.getProperty(name));
                } else if (StringUtils.equals(name, "smtpServer")) {
                    _notificationsPrefs.setSmtpServer(_serializer.deserializeJson(properties.getProperty(name), SmtpServer.class));
                } else {
                    _notificationsPrefs.set(properties.getProperty(name), name);
                }
                log.info("Set property {} to value: {}", name, properties.get(name));
            } catch (InvalidPreferenceName invalidPreferenceName) {
                log.error("Got an invalid preference name error for the preference '{}', which is weird because the site configuration is not strict", name);
            } catch (IOException e) {
                log.error("An error occurred deserializing the preference '{}', which is just lame.", name);
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Returns the full SMTP server configuration.", description = "Returns the configuration as a map of the standard Java mail sender properties&ndash;host, port, protocol, username, and password&ndash;along with any extended properties required for the configuration, e.g. configuring SSL- or TLS-secured SMTP services.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "SMTP service configuration properties successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set site configuration properties."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "smtp", produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<Properties> getSmtpServerProperties() {
        log.debug("User {} requested the site SMTP service configuration.", getSessionUser().getUsername());
        return new ResponseEntity<>(_notificationsPrefs.getSmtpServer().asProperties(), HttpStatus.OK);
    }

    @Operation(summary = "Sets the mail service properties. This return the SMTP server configuration as it exists after being set.", description = POST_PROPERTIES_NOTES)
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Mail service properties successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set the mail service properties."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"smtp"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setAllMailProperties(@Parameter(description = "The value to set for the email host.") @RequestParam(value = "hostname", required = false, defaultValue = NOT_SET) final String hostname,
                                                           @Parameter(description = "The value to set for the email port.") @RequestParam(value = "port", required = false, defaultValue = "-1") final int port,
                                                           @Parameter(description = "The value to set for the email username.") @RequestParam(value = "username", required = false, defaultValue = NOT_SET) final String username,
                                                           @Parameter(description = "The value to set for the email password.") @RequestParam(value = "password", required = false, defaultValue = NOT_SET) final String password,
                                                           @Parameter(description = "The value to set for the email protocol.") @RequestParam(value = "protocol", required = false, defaultValue = NOT_SET) final String protocol,
                                                           @Parameter(description = "Values to set for extra mail properties. An empty value indicates that an existing property should be removed.") @RequestParam final Properties properties) {
        cleanProperties(properties);
        logConfigurationSubmit(hostname, port, username, password, protocol, properties);

        _notificationsPrefs.setSmtpServer(new SmtpServer(hostname, port, protocol, username, password, properties));

        return getSmtpServerProperties();
    }

    @Operation(summary = "Sets the submitted mail service properties. This returns the SMTP server configuration as it exists after being set.", description = PUT_PROPERTIES_NOTES)
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Mail service properties successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set the mail service properties."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"smtp"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.PUT}, restrictTo = Admin)
    public ResponseEntity<Properties> setSubmittedMailProperties(@Parameter(description = "The value to set for the email host.") @RequestParam(value = "hostname", required = false, defaultValue = NOT_SET) final String host,
                                                                 @Parameter(description = "The value to set for the email port.") @RequestParam(value = "port", required = false, defaultValue = "-1") final int port,
                                                                 @Parameter(description = "The value to set for the email username.") @RequestParam(value = "username", required = false, defaultValue = NOT_SET) final String username,
                                                                 @Parameter(description = "The value to set for the email password.") @RequestParam(value = "password", required = false, defaultValue = NOT_SET) final String password,
                                                                 @Parameter(description = "The value to set for the email protocol.") @RequestParam(value = "protocol", required = false, defaultValue = NOT_SET) final String protocol,
                                                                 @Parameter(description = "Values to set for extra mail properties. An empty value indicates that an existing property should be removed.") @RequestParam final Properties properties) {
        logConfigurationSubmit(host, port, username, password, protocol, properties);

        _notificationsPrefs.setSmtpServer(new SmtpServer(host, port, username, password, protocol, properties));

        return getSmtpServerProperties();
    }

    @Operation(summary = "Sets the mail service host.", description = "Sets the mail service host.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Mail service host successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set the mail service host."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"smtp/host/{host}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.PUT}, restrictTo = Admin)
    public ResponseEntity<Properties> setHostProperty(@Parameter(description = "The value to set for the email host.", required = true) @PathVariable final String host) {
        log.info("User {} setting mail host to: {}", getSessionUser().getLogin(), host);
        _notificationsPrefs.setSmtpHostname(host);
        return getSmtpServerProperties();
    }

    @Operation(summary = "Sets the mail service port.", description = "Sets the mail service port.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Mail service port successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set the mail service port."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"smtp/port/{port}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.PUT}, restrictTo = Admin)
    public ResponseEntity<Properties> setPortProperty(@Parameter(description = "The value to set for the email port.", required = true) @PathVariable final int port) {
        log.info("User {} setting mail port to: {}", getSessionUser().getLogin(), port);
        _notificationsPrefs.setSmtpPort(port);
        return getSmtpServerProperties();
    }

    @Operation(summary = "Sets the mail service protocol.", description = "Sets the mail service protocol.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Mail service protocol successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set the mail service protocol."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"smtp/protocol/{protocol}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.PUT}, restrictTo = Admin)
    public ResponseEntity<Properties> setProtocolProperty(@Parameter(description = "The value to set for the email protocol.", required = true) @PathVariable final String protocol) {
        log.info("User {} setting mail protocol to: {}", getSessionUser().getLogin(), protocol);
        _notificationsPrefs.setSmtpProtocol(protocol);
        return getSmtpServerProperties();
    }

    @Operation(summary = "Sets the mail service username.", description = "Sets the mail service username.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Mail service username successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set the mail service username."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"smtp/username/{username}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.PUT}, restrictTo = Admin)
    public ResponseEntity<Properties> setUsernameProperty(@Parameter(description = "The value to set for the email username.", required = true) @PathVariable final String username) {
        log.info("User {} setting mail username to: {}", getSessionUser().getUsername(), username);
        _notificationsPrefs.setSmtpUsername(username);
        return getSmtpServerProperties();
    }

    @Operation(summary = "Sets the mail service password.", description = "Sets the mail service password.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Mail service password successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set the mail service password."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"smtp/password/{password}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.PUT}, restrictTo = Admin)
    public ResponseEntity<Properties> setPasswordProperty(@Parameter(description = "The value to set for the email password.", required = true) @PathVariable final String password) {
        log.info("User {} setting mail password", getSessionUser().getUsername());
        _notificationsPrefs.setSmtpPassword(password);
        return getSmtpServerProperties();
    }

    @Operation(summary = "Gets the value for a specified Java mail property.", description = "The value is always returned as a string.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Property found and returned."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set the mail service properties."),
                   @ApiResponse(responseCode = "404", description = "Specified key not found in the mail service properties."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"smtp/property/{property}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getExtendedProperty(@Parameter(description = "The mail property to be retrieved.", required = true) @PathVariable final String property) {
        if (!_notificationsPrefs.getSmtpServer().getMailProperties().containsKey(property)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.info("User {} retrieving value for property {}", getSessionUser().getLogin(), property);
        return new ResponseEntity<>(_notificationsPrefs.getSmtpMailProperty(property), HttpStatus.OK);
    }

    @Operation(summary = "Sets a Java mail property with the submitted name and value.", description = "Setting a property to an existing value will overwrite the existing value. The value returned by this function contains the previous value (if any).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Property found and returned."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set the mail service properties."),
                   @ApiResponse(responseCode = "404", description = "Specified key not found in the mail service properties."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"smtp/property/{property}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.PUT}, restrictTo = Admin)
    public ResponseEntity<String> setExtendedProperty(@Parameter(description = "The name of the extended mail property to set.", required = true) @PathVariable final String property,
                                                      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The value to set for the extended mail property.", required = true) @RequestBody final String value) {
        return setExtendedPropertyFromPath(property, value);
    }

    @Operation(summary = "Sets a Java mail property with the submitted name and value.", description = "Setting a property to an existing value will overwrite the existing value. The value returned by this function contains the previous value (if any).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Mail service password successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set the mail service password."),
                   @ApiResponse(responseCode = "404", description = "Specified key not found in the mail service properties."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"smtp/property/{property}/{value}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.PUT}, restrictTo = Admin)
    public ResponseEntity<String> setExtendedPropertyFromPath(@Parameter(description = "The name of the extended mail property to set.", required = true) @PathVariable final String property,
                                                              @Parameter(description = "The value to set for the extended mail property.", required = true) @PathVariable final String value) {
        log.info("User {} setting mail password", getSessionUser().getUsername());
        return new ResponseEntity<>(_notificationsPrefs.setSmtpMailProperty(property, value), HttpStatus.OK);
    }

    @Operation(summary = "Removes the value for a specified Java mail property.", description = "This completely removes the specified mail property. The value returned by this function contains the previous value (if any).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Property found and returned."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set the mail service properties."),
                   @ApiResponse(responseCode = "404", description = "Specified key not found in the mail service properties."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "smtp/property/{property}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE, restrictTo = Admin)
    public ResponseEntity<String> removeExtendedProperty(@Parameter(description = "The mail property to be removed.", required = true) @PathVariable final String property) {
        if (!_notificationsPrefs.getSmtpServer().getMailProperties().containsKey(property)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.info("User {} removing value for property {}", getSessionUser().getLogin(), property);
        return new ResponseEntity<>(_notificationsPrefs.removeSmtpMailProperty(property), HttpStatus.OK);
    }

    @Operation(summary = "Sets the email message for contacting help.", description = "Sets the email message that people should receive when contacting help.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Help email message successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set the help email message."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/help"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Void> setHelpContactInfo(@Parameter(description = "The email message for contacting help.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setHelpContactInfo(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email message for user registration.", description = "Sets the email message that people should receive when they register. Link for email validation is auto-populated.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User registration email message successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set the user registration email message."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/registration"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessageUserRegistration(@Parameter(description = "The email message for user registration.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessageUserRegistration(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email message for forgot username.", description = "Sets the email message that people should receive when they click that they forgot their username.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Forgot username email message successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set the forgot username email message."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/forgotusername"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessageForgotUsernameRequest(@Parameter(description = "The email message for forgot username.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessageForgotUsernameRequest(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email message for password reset.", description = "Sets the email message that people should receive when they click to reset their password.  Link for password reset is auto-populated.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Password reset message successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set the password reset message."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/passwordreset"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessageForgotPasswordReset(@Parameter(description = "The email message for password reset.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessageForgotPasswordReset(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email message for new user verification.", description = "Sets the email message that people should receive when are requested to verify their email address for a new account. Link should be auto-populated.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User verification email message successfully set."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to set the user verification email message."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/newuserverification"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessageNewUserVerification(@Parameter(description = "The email message for user registration.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessageNewUserVerification(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email message for a user project access request.", description = "Sets the email message that owners/members/etc. should receive when a user requests access to a project. Link should be auto-populated.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Project access request email message successfully set."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to set the project access request email message."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/projectaccessrequest"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessageProjectAccessRequest(@Parameter(description = "The email message for project access request.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessageProjectRequestAccess(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email message for approving a project access request.", description = "Sets the email message that a new user should receive when they have received approval to access a project. Link should be auto-populated.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Project access approval email message successfully set."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to set the project access approval email message."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/approveprojectaccess"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessageApproveProjectAccess(@Parameter(description = "The email message for project access approval.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessageProjectAccessApproval(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email message for denying a project access request.", description = "Sets the email message that a new user should receive when they have been denied in their request to access a project. Link should be auto-populated.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Project access denial email message successfully set."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to set the project access denial email message."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/denyprojectaccess"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessageDenyProjectAccess(@Parameter(description = "The email message for project access denial.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessageProjectAccessDenial(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email message for inviting a new user to the project by email address.", description = "Sets the email message that a new user should receive when they have been invited to a new project by email address. Link should be auto-populated.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Project access invite email message successfully set."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to set the project access invite email message."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/inviteprojectacess"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessageProjectAccessInvite(@Parameter(description = "The email message for project access invite.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessageInviteProjectAccess(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email message sent after a user re-enables an account disabled due to inactivity.", description = "Link should be auto-populated.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Disabled user account verification email message successfully set."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to set the email message for disabled account user verification."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/diableduseraccountverification"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessageDisabledAccountUserVerification(@Parameter(description = "The new email message for disabled account user verification.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessageDisabledUserVerification(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email message sent when an error occurs.", description = "Link should be auto-populated.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Email message sent when an error occurs successfully set."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to set the error email message."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/error"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessageError(@Parameter(description = "The new email message to be sent when an error occurs.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessageErrorMessage(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email message sent to subscribers when a new user has been created.", description = "Link should be auto-populated.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Email message sent to subscribers upon new user creation successfully set."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to set the new user notification email message."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/newusernotification"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessageNewUserNotification(@Parameter(description = "The new email message to be sent to subscribers when new users are created.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessageNewUserNotification(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email message sent to subscribers when a new user account has been created but auto-enable has been turned off.", description = "Link should be auto-populated.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Email message sent to subscribers upon new user request successfully set."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to set the new user request email message."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/newuserrequest"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessageNewUserRequest(@Parameter(description = "The new email message to be sent to subscribers when a new user account has been created but auto-enable has been turned off.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessageNewUserRequest(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email message sent out when an experiment pipeline has completed successfully without errors.", description = "Link should be auto-populated.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Email message sent upon pipeline success successfully set."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to set the pipeline success email message."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/pipelinesuccessdefault"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessagePipelineSuccessDefault(@Parameter(description = "The new email message to be sent when an experiment pipeline has completed successfully without errors.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessagePipelineDefaultSuccess(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email message sent out when an experiment pipeline fails before completion due to error.", description = "Link should be auto-populated.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Pipeline failure email message successfully set."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to set the pipeline failure email message."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/pipelinefailure"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessagePipelineFailure(@Parameter(description = "The new email message sent out when an experiment pipeline fails before completion due to error.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessagePipelineDefaultFailure(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email message sent out when an automated pipeline is run successfully and a session is archived in the system.", description = "Link should be auto-populated.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Pipeline autorun success email message successfully set."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to set the pipeline autorun success email message."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/pipelinesuccessautorun"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessagePipelineSuccessAutorun(@Parameter(description = "The new email message sent out when an automated pipeline is run successfully and a session is archived in the system.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessagePipelineAutorunSuccess(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email message when a batch data transfer to the archive has been successfully completed.", description = "Link should be auto-populated.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Batch transfer email message successfully set."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to set the batch transfer email message."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/batchtransfer"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessageBatchTransfer(@Parameter(description = "The new email message sent out when a batch data transfer to the archive has been successfully completed.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessageBatchWorkflowComplete(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email message sent when one of several types of unauthorized data or login access attempts are recognized by the system.", description = "Individual type of access attempt should automatically be populated based on the situation.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Unauthorized data attempt message successfully set."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to set the unauthorized data attempt email message."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/unauthorizeddataattempt"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessageUnauthorizedDataAttempt(@Parameter(description = "The new email message sent when one of several types of unauthorized data or login access attempts are recognized by the system.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessageUnauthorizedDataAttempt(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email message sent to both the former and new email address associated with an account to alert the user that the email has successfully been changed.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Account email change request email message successfully set."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to set email message for an email address change request."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/emailchangerequest"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessageEmailChangedRequest(@Parameter(description = "The new email message sent to both the former and new email address associated with an account to alert the user that the email has successfully been changed.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessageEmailAddressChangeRequest(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email message sent to a new email address for a user to verify the change made to the account's associated email.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Verify new email address email message successfully set."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to set email message for verification of a new account email address."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/emailchangeverification"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessageEmailChangeVerification(@Parameter(description = "The new email message sent to a new email address for a user to verify the change made to the account's associated email.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessageVerifyEmailChangeRequest(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email message sent to both the former and new email address associated with an account to alert the user that the email has successfully been changed.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Confirm account email address changed email message successfully set."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to set email message for confirmation of an account email address change."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/emailadresschangedconfirmation"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailAddressChangedConfirmation(@Parameter(description = "The new email message sent to both the former and new email address associated with an account to alert the user that the email has successfully been changed.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessageAddressChanged(message);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @Operation(summary = "Sets the email message sent when XNAT is unable to find the filesystem (e.g,, archive, build, prearchive directories).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "System path error email message successfully set."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to set email message for system path errors."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/systempatherror"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessageSystemPathError(@Parameter(description = "The new email message sent when XNAT is unable to find the filesystem (e.g,, archive, build, prearchive directories).", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessageSystemPathError(message);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @Operation(summary = "Sets the email message sent when a user has uploaded a file or files to the system by reference.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Upload by reference email message successfully set."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to set email message for upload by reference notification."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/uploadbyreference"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessageUploadByReference(@Parameter(description = "The new email message sent when a user has uploaded a file or files to the system by reference.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessageSystemPathError(message);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @Operation(summary = "Sets the email message sent when a user has attempted to upload a file or files to the system by reference but the process failed.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Upload by reference failure email message successfully set."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to set email message for upload by reference failure notification."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/uploadbyreferencefailure"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessageUploadByReferenceFailure(@Parameter(description = "The new email message sent when a user has attempted to upload a file or files to the system by reference but the process failed.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessageUploadByReferenceFailure(message);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @Operation(summary = "Sets the email message sent out by from one user to another to alert them of possibly useful data.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Data alert email message successfully set."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to set email message for data alerts."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/dataalertcustom"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailMessageDataAlertCustom(@Parameter(description = "The new email message sent out by from one user to another to alert them of possibly useful data.", required = true) @RequestParam final String message) {
        _notificationsPrefs.setEmailMessageDataAlertCustom(message);
        return new ResponseEntity<>(HttpStatus.OK);

    }


    @Operation(summary = "Resets the email message for the specified api call to its original value.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Email message successfully reset."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to reset email message."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/resetemailmessage"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> resetEmailMessage(@Parameter(description = "The API call corresponding to the email which should be reset.", required = true) @RequestParam final String apiToReset) {
        if (apiToReset.equals("registration")) {
            _notificationsPrefs.resetEmailMessageUserRegistration();
        } else if (apiToReset.equals("forgotusername")) {
            _notificationsPrefs.resetEmailMessageForgotUsername();
        } else if(apiToReset.equals("passwordreset")) {
            _notificationsPrefs.resetEmailMessageForgotPassword();
        } else if (apiToReset.equals("newuserverification")) {
            _notificationsPrefs.resetEmailMessageNewUserVerification();
        } else if (apiToReset.equals("projectaccessrequest")) {
            _notificationsPrefs.resetEmailMessageProjectRequestAccess();
        } else if (apiToReset.equals("approveprojectaccess")) {
            _notificationsPrefs.resetEmailMessageProjectAccessApproval();
        } else if (apiToReset.equals("denyprojectaccess")) {
            _notificationsPrefs.resetEmailMessageProjectAccessDenial();
        } else if (apiToReset.equals("inviteprojectacess")) {
            _notificationsPrefs.resetEmailMessageInviteProjectAccess();
        } else if (apiToReset.equals("diableduseraccountverification")){
            _notificationsPrefs.resetEmailMessageDisabledUserVerification();
        } else if (apiToReset.equals("newusernotification")) {
            _notificationsPrefs.resetEmailMessageNewUserNotification();
        } else if (apiToReset.equals("newuserrequest")) {
            _notificationsPrefs.resetEmailMessageNewUserRequest();
        } else if (apiToReset.equals("pipelinesuccessdefault")){
            _notificationsPrefs.resetEmailMessagePipelineDefaultSuccess();
        } else if (apiToReset.equals("pipelinefailure")) {
            _notificationsPrefs.resetEmailMessagePipelineDefaultFailure();
        } else if (apiToReset.equals("pipelinesuccessautorun")) {
            _notificationsPrefs.resetEmailMessagePipelineAutorunSuccess();
        } else if(apiToReset.equals("batchtransfer")){
            _notificationsPrefs.resetEmailMessageBatchWorkflowComplete();
        } else if (apiToReset.equals("unauthorizeddataattempt")) {
            _notificationsPrefs.resetEmailMessageUnauthorizedDataAttempt();
        } else if (apiToReset.equals("emailchangerequest")) {
            _notificationsPrefs.resetEmailMessageEmailAddressChangeRequest();
        } else if (apiToReset.equals("emailchangeverification")) {
            _notificationsPrefs.resetEmailMessageVerifyEmailChangeRequest();
        } else if (apiToReset.equals("emailadresschangedconfirmation")) {
            _notificationsPrefs.resetEmailMessageAddressChanged();
        } else if (apiToReset.equals("systempatherror")) {
            _notificationsPrefs.resetEmailMessageSystemPathError();
        } else if (apiToReset.equals("uploadbyreference")) {
            _notificationsPrefs.resetEmailMessageUploadByReferenceSuccess();
        } else if (apiToReset.equals("uploadbyreferencefailure")) {
            _notificationsPrefs.resetEmailMessageUploadByReferenceFailure();
        } else if (apiToReset.equals("dataalertcustom")) {
            _notificationsPrefs.resetEmailMessageDataAlertCustom();
        }

        else if(apiToReset.equals("error")) {
            _notificationsPrefs.resetEmailMessageErrorMessage();
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message for contacting help.", description = "This returns the email message that people should receive when contacting help.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Email message for contacting help successfully returned."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to get email message for contacting help."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/help"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getHelpContactInfo() {
        return new ResponseEntity<>(_notificationsPrefs.getHelpContactInfo(), HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message for user registration.", description = "This returns the email message that people should receive when they register. Link for email validation is auto-populated.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Email message for user registration successfully returned."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to get email message for user registration."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/registration"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessageUserRegistration() {
        return new ResponseEntity<>(_notificationsPrefs.getEmailMessageUserRegistration(), HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message for forgot username.", description = "This returns the email message that people should receive when they click that they forgot their username.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Email message for forgot username successfully returned."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to get email message for forgot username."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/forgotusername"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessageForgotUsernameRequest() {
        return new ResponseEntity<>(_notificationsPrefs.getEmailMessageForgotUsernameRequest(), HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message for password reset.", description = "This returns the email message that people should receive when they click to reset their password.  Link for password reset is auto-populated.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Email message for password reset successfully returned."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to get email message for password reset."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/passwordreset"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessageForgotPasswordReset() {
        String resp = _notificationsPrefs.getEmailMessageForgotPasswordReset();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message for new user verification.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Email message for new user verification successfully returned."),
                @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                @ApiResponse(responseCode = "403", description = "Not authorized to get email message for new user verification."),
                @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/newuserverification"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessageNewUserVerification() throws Exception{
        String resp = _notificationsPrefs.getEmailMessageNewUserVerification();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message for a user project access request.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Email message for project access request successfully returned."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to get email message for project access request."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/projectaccessrequest"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessageProjectAccessRequest() {
        String resp = _notificationsPrefs.getEmailMessageProjectRequestAccess();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message for approving a project access request.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Project access approval email message successfully returned."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to get email message for project access approval."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/approveprojectaccess"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessageApproveProjectAccess() {
        String resp = _notificationsPrefs.getEmailMessageProjectAccessApproval();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message for denying a project access request.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Project access denial email message successfully returned."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to get email message for project access denial."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/denyprojectaccess"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessageDenyProjectAccess() {
        String resp = _notificationsPrefs.getEmailMessageProjectAccessDenial();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message for inviting a new user to the project by email address.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Project access invite email message successfully returned."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to get email message for project access invite."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/inviteprojectacess"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessageProjectAccessInvite() {
        String resp = _notificationsPrefs.getEmailMessageInviteProjectAccess();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message sent after a user re-enables an account disabled due to inactivity.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Disabled account user verification email message successfully returned."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to get email message for disabled account user verification."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/diableduseraccountverification"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessageDisabledAccountUserVerification() {
        String resp = _notificationsPrefs.getEmailMessageDisabledUserVerification();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message sent when an error occurs.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Error email message successfully returned."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to get email message for errors occurring within the system."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/error"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessageError() {
        String resp = _notificationsPrefs.getEmailMessageErrorMessage();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message sent to subscribers when a new user has been created.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "New user created email message successfully returned."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to get email message for new user creation."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/newusernotification"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessageNewUserNotification() {
        String resp = _notificationsPrefs.getEmailMessageNewUserNotification();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message sent to subscribers when a new user account has been created but auto-enable has been turned off.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "New user requested email message successfully returned."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to get email message for new user requests."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/newuserrequest"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessageNewUserRequest() {
        String resp = _notificationsPrefs.getEmailMessageNewUserRequest();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message sent out when an experiment pipeline has completed successfully without errors.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Pipeline success email message successfully returned."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to get email message for experiment pipeline success."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/pipelinesuccessdefault"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessagePipelineSuccessDefault() {
        String resp = _notificationsPrefs.getEmailMessagePipelineDefaultSuccess();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message sent out when an experiment pipeline fails before completion due to error.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Pipeline failure email message successfully returned."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to get email message for pipeline failure."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/pipelinefailure"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessagePipelineFailure() {
        String resp = _notificationsPrefs.getEmailMessagePipelineDefaultFailure();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message sent out when an automated pipeline is run successfully and a session is archived in the system.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Pipeline autorun success email message successfully returned."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to get email message for pipeline autorun success."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/pipelinesuccessautorun"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessagePipelineSuccessAutorun() {
        String resp = _notificationsPrefs.getEmailMessagePipelineAutorunSuccess();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message when a batch data transfer to the archive has been successfully completed.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Batch transfer completed email message successfully returned."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to get email message for batch transfer completed."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/batchtransfer"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessageBatchTransfer() {
        String resp = _notificationsPrefs.getEmailMessageBatchWorkflowComplete();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message sent when one of several types of unauthorized data or login access attempts are recognized by the system.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Unauthorized data attempt email message successfully returned."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to get email message for unauthorized data attempt."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/unauthorizeddataattempt"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessageUnauthorizedDataAttempt() {
        String resp = _notificationsPrefs.getEmailMessageUnauthorizedDataAttempt();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message sent to alert a user that someone has requested to change the email for the account associated with that email address.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Account email change request email message successfully returned."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to get email message for an email address change request."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/emailchangerequest"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessageEmailChangedRequest() {
        String resp = _notificationsPrefs.getEmailMessageEmailAddressChangeRequest();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message sent to a new email address for a user to verify the change made to the account's associated email.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Verify new email address email message successfully returned."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to get email message for verification of a new account email address."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/emailchangeverification"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessageEmailChangeVerification() {
        String resp = _notificationsPrefs.getEmailMessageVerifyEmailChangeRequest();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message sent to both the former and new email address associated with an account to alert the user that the email has successfully been changed.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Confirm account email address changed email message successfully returned."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to get email message for confirmation of an account email address change."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/emailadresschangedconfirmation"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getMessageEmailAddressChangedConfirmation() {
        String resp = _notificationsPrefs.getEmailMessageAddressChanged();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message sent when XNAT is unable to find the filesystem (e.g,, archive, build, prearchive directories).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "System path error email message successfully returned."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to get email message for system path errors."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/systempatherror"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessageSystemPathError() {
        String resp = _notificationsPrefs.getEmailMessageSystemPathError();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message sent when a user has uploaded a file or files to the system by reference.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Upload by reference email message successfully returned."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to get email message for upload by reference notification."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/uploadbyreference"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessageUploadByReference() {
        String resp = _notificationsPrefs.getEmailMessageUploadByReferenceSuccess();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message sent when a user has attempted to upload a file or files to the system by reference but the process failed.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Upload by reference failure email message successfully returned."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to get email message for upload by reference failure notification."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/uploadbyreferencefailure"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessageUploadByReferenceFailure() {
        String resp = _notificationsPrefs.getEmailMessageUploadByReferenceFailure();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Returns the email message sent out by from one user to another to alert them of possibly useful data.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Data alert email message successfully returned."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to get email message for data alerts."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"messages/dataalertcustom"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getEmailMessageDataAlertCustom() {
        String resp = _notificationsPrefs.getEmailMessageDataAlertCustom();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Operation(summary = "Sets whether admins should be notified of user registration.", description = "Sets whether admins should be notified of user registration.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Whether admins should be notified of user registration successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set whether admins should be notified of user registration."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"notify/registration"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Void> setNotifyAdminUserRegistration(@Parameter(description = "Whether admins should be notified of user registration successfully set.", required = true) @RequestParam final boolean notify) {
        _notificationsPrefs.setNotifyAdminUserRegistration(notify);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets whether admins should be notified of pipeline processing submit.", description = "Sets whether admins should be notified of pipeline processing submit.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Whether admins should be notified of pipeline processing submit successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set whether admins should be notified of pipeline processing submit."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"notify/pipeline"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setNotifyAdminPipelineEmails(@Parameter(description = "Whether admins should be notified of pipeline processing submit successfully set.", required = true) @RequestParam final boolean notify) {
        _notificationsPrefs.setNotifyAdminPipelineEmails(notify);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets whether admins should be notified of project access requests.", description = "Sets whether admins should be notified of project access requests.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Whether admins should be notified of project access requests successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set whether admins should be notified of project access requests."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"notify/par"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setNotifyAdminProjectAccessRequest(@Parameter(description = "Whether admins should be notified of project access requests successfully set.", required = true) @RequestParam final boolean notify) {
        _notificationsPrefs.setNotifyAdminProjectAccessRequest(notify);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets whether admins should be notified of session transfer.", description = "Sets whether admins should be notified of session transfer by user.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Whether admins should be notified of session transfer successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set whether admins should be notified of session transfer."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"notify/transfer"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setNotifyAdminSessionTransfer(@Parameter(description = "Whether admins should be notified of session transfer successfully set.", required = true) @RequestParam final boolean notify) {
        _notificationsPrefs.setNotifyAdminSessionTransfer(notify);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Returns whether admins should be notified of user registration.", description = "This returns whether admins should be notified of user registration.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Email message for contacting help successfully returned."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to get email message for contacting help."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"notify/registration"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<Boolean> getNotifyAdminUserRegistration() {
        return new ResponseEntity<>(_notificationsPrefs.getNotifyAdminUserRegistration(), HttpStatus.OK);
    }

    @Operation(summary = "Returns whether admins should be notified of pipeline processing submit.", description = "This returns whether admins should be notified of pipeline processing submit.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Email message for user registration successfully returned."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to get email message for user registration."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"notify/pipeline"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<Boolean> getNotifyAdminPipelineEmails() {
        return new ResponseEntity<>(_notificationsPrefs.getNotifyAdminPipelineEmails(), HttpStatus.OK);
    }

    @Operation(summary = "Returns whether admins should be notified of project access requests.", description = "This returns whether admins should be notified of project access requests.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Email message for forgot username successfully returned."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to get email message for forgot username."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"notify/par"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<Boolean> getNotifyAdminProjectAccessRequest() {
        return new ResponseEntity<>(_notificationsPrefs.getNotifyAdminProjectAccessRequest(), HttpStatus.OK);
    }

    @Operation(summary = "Returns whether admins should be notified of session transfer.", description = "This returns whether admins should be notified of session transfer.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Email message for password reset successfully returned."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to get email message for password reset."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"notify/transfer"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<Boolean> getNotifyAdminSessionTransfer() {
        return new ResponseEntity<>(_notificationsPrefs.getNotifyAdminSessionTransfer(), HttpStatus.OK);
    }

    @Operation(summary = "Sets whether non-users should be able to subscribe to notifications.", description = "Sets whether non-users should be able to subscribe to notifications.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Whether non-users should be able to subscribe to notifications."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set whether non-users should be able to subscribe to notifications."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"allow/nonusersubscribers/{setting}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setEmailAllowNonuserSubscribers(@Parameter(description = "Whether non-users should be able to subscribe to notifications.", required = true) @PathVariable final boolean setting) {
        _notificationsPrefs.setEmailAllowNonuserSubscribers(setting);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Returns whether non-users should be able to subscribe to notifications.", description = "This returns whether non-users should be able to subscribe to notifications.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Whether non-users should be able to subscribe to notifications successfully returned."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to get whether non-users should be able to subscribe to notifications."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"allow/nonusersubscribers"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<Boolean> getEmailAllowNonuserSubscribers() {
        return new ResponseEntity<>(_notificationsPrefs.getEmailAllowNonuserSubscribers(), HttpStatus.OK);
    }

    @Operation(summary = "Sets the email addresses for error notifications.", description = "Sets the email addresses that should be subscribed to error notifications.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Error subscribers successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set the error subscribers."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"subscribers/error"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Void> setErrorSubscribers(@Parameter(description = "The values to set for email addresses for error notifications.", required = true) @RequestParam final String subscribers) {
        _notificationsPrefs.setEmailRecipientErrorMessages(subscribers);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email addresses for issue notifications.", description = "Sets the email addresses that should be subscribed to issue notifications.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Issue subscribers successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set the issue subscribers."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"subscribers/issue"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setIssueSubscribers(@Parameter(description = "The values to set for email addresses for issue notifications.", required = true) @RequestParam final String subscribers) {
        _notificationsPrefs.setEmailRecipientIssueReports(subscribers);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email addresses for new user notifications.", description = "Sets the email addresses that should be subscribed to new user notifications.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "New user subscribers successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set the new user subscribers."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"subscribers/newuser"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setNewUserSubscribers(@Parameter(description = "The values to set for email addresses for new user notifications.", required = true) @RequestParam final String subscribers) {
        _notificationsPrefs.setEmailRecipientNewUserAlert(subscribers);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Sets the email addresses for update notifications.", description = "Sets the email addresses that should be subscribed to update notifications.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Update subscribers successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set the update subscribers."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"subscribers/update"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST}, restrictTo = Admin)
    public ResponseEntity<Properties> setUpdateSubscribers(@Parameter(description = "The values to set for email addresses for update notifications.", required = true) @RequestParam final String subscribers) {
        _notificationsPrefs.setEmailRecipientUpdate(subscribers);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Returns list of email addresses subscribed to error notifications.", description = "This returns a list of all the email addresses that are subscribed to receive error notifications.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Error notification subscribers successfully returned."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to get subscribers for email notifications."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"subscribers/error"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getErrorSubscribers() {
        return new ResponseEntity<>(_notificationsPrefs.getEmailRecipientErrorMessages(), HttpStatus.OK);
    }

    @Operation(summary = "Returns list of email addresses subscribed to issue notifications.", description = "This returns a list of all the email addresses that are subscribed to receive issue notifications.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Issue notification subscribers successfully returned."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to get subscribers for email notifications."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"subscribers/issue"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getIssueSubscribers() {
        return new ResponseEntity<>(_notificationsPrefs.getEmailRecipientIssueReports(), HttpStatus.OK);
    }

    @Operation(summary = "Returns list of email addresses subscribed to new user notifications.", description = "This returns a list of all the email addresses that are subscribed to receive new user notifications.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "New user notification subscribers successfully returned."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to get subscribers for email notifications."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"subscribers/newuser"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getNewUserSubscribers() {
        return new ResponseEntity<>(_notificationsPrefs.getEmailRecipientNewUserAlert(), HttpStatus.OK);
    }

    @Operation(summary = "Returns list of email addresses subscribed to update notifications.", description = "This returns a list of all the email addresses that are subscribed to receive update notifications.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Update notification subscribers successfully returned."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to get subscribers for email notifications."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"subscribers/update"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET}, restrictTo = Admin)
    public ResponseEntity<String> getUpdateSubscribers() {
        return new ResponseEntity<>(_notificationsPrefs.getEmailRecipientUpdate(), HttpStatus.OK);
    }

    private void cleanProperties(final Properties properties) {
        for (final String property : PROPERTIES_TO_CLEAN) {
            properties.remove(property);
        }
    }

    private void logConfigurationSubmit(final String host, final int port, final String username, final String password, final String protocol, final Properties properties) {
        if (log.isInfoEnabled()) {
            final StringBuilder message = new StringBuilder("User ");
            message.append(getSessionUser().getLogin()).append(" setting mail properties to:\n");
            message.append(" * Host:     ").append(StringUtils.equals(NOT_SET, host) ? "No value submitted..." : host).append("\n");
            message.append(" * Port:     ").append(port == -1 ? "No value submitted..." : port).append("\n");
            message.append(" * Protocol: ").append(StringUtils.equals(NOT_SET, protocol) ? "No value submitted..." : protocol).append("\n");
            message.append(" * Username: ").append(StringUtils.equals(NOT_SET, username) ? "No value submitted..." : username).append("\n");
            message.append(" * Password: ").append(StringUtils.equals(NOT_SET, password) ? "No value submitted..." : "********").append("\n");
            if (properties != null && properties.size() > 0) {
                for (final String property : properties.stringPropertyNames()) {
                    message.append(" * ").append(property).append(": ").append(properties.get(property)).append("\n");
                }
            }
            log.info(message.toString());
        }
    }

    private static final String       NOT_SET             = "NotSet";
    private static final List<String> PROPERTIES_TO_CLEAN = Arrays.asList("hostname", "port", "protocol", "username", "password", "smtpHostname", "smtpPort", "smtpProtocol", "smtpUsername", "smtpPassword");

    private final NotificationsPreferences _notificationsPrefs;
    private final XnatAppInfo              _appInfo;
    private final SerializerService        _serializer;
}
