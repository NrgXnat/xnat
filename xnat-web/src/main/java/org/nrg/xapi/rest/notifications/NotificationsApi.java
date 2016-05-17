package org.nrg.xapi.rest.notifications;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.mail.api.NotificationType;
import org.nrg.notify.api.CategoryScope;
import org.nrg.notify.api.SubscriberType;
import org.nrg.notify.entities.*;
import org.nrg.notify.exceptions.DuplicateDefinitionException;
import org.nrg.notify.exceptions.DuplicateSubscriberException;
import org.nrg.notify.services.NotificationService;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.rest.AbstractXnatRestApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import java.util.*;

@Api(description = "XNAT Notifications management API")
@XapiRestController
@RequestMapping(value = "/notifications")
public class NotificationsApi extends AbstractXnatRestApi {
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

    @ApiOperation(value = "Returns the full SMTP server configuration.", notes = "Returns the configuration as a map of the standard Java mail sender properties&ndash;host, port, protocol, username, and password&ndash;along with any extended properties required for the configuration, e.g. configuring SSL- or TLS-secured SMTP services.", response = Properties.class)
    @ApiResponses({@ApiResponse(code = 200, message = "SMTP service configuration properties successfully retrieved."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set site configuration properties."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = "smtp", produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<Properties> getSmtpServerProperties() {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        if (_log.isDebugEnabled()) {
            _log.debug("User " + getSessionUser().getUsername() + " requested the site SMTP service configuration.");
        }
        final Properties properties = new Properties();
        properties.setProperty("host", _javaMailSender.getHost());
        final int port = _javaMailSender.getPort();
        if (port > 0) {
            properties.setProperty("port", Integer.toString(port));
        }
        final String protocol = _javaMailSender.getProtocol();
        if (StringUtils.isNotBlank(protocol)) {
            properties.setProperty("protocol", protocol);
        }
        final String username = _javaMailSender.getUsername();
        if (StringUtils.isNotBlank(username)) {
            properties.setProperty("username", username);
        }
        final String password = _javaMailSender.getPassword();
        if (StringUtils.isNotBlank(password)) {
            properties.setProperty("password", password);
        }
        for (final String property : _javaMailSender.getJavaMailProperties().stringPropertyNames()) {
            properties.setProperty(property, _javaMailSender.getJavaMailProperties().getProperty(property));
        }
        return new ResponseEntity<>(properties, HttpStatus.OK);
    }

    @ApiOperation(value = "Sets the mail service properties. This return the SMTP server configuration as it exists after being set.", notes = POST_PROPERTIES_NOTES, response = Properties.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Mail service properties successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the mail service properties."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"smtp"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST})
    public ResponseEntity<Properties> setAllMailProperties(@ApiParam("The value to set for the email host.") @RequestParam(value = "host", required = false, defaultValue = NOT_SET) final String host,
                                                           @ApiParam("The value to set for the email port.") @RequestParam(value = "port", required = false, defaultValue = "-1") final int port,
                                                           @ApiParam("The value to set for the email username.") @RequestParam(value = "username", required = false, defaultValue = NOT_SET) final String username,
                                                           @ApiParam("The value to set for the email password.") @RequestParam(value = "password", required = false, defaultValue = NOT_SET) final String password,
                                                           @ApiParam("The value to set for the email protocol.") @RequestParam(value = "protocol", required = false, defaultValue = NOT_SET) final String protocol,
                                                           @ApiParam(value = "Values to set for extra mail properties. An empty value indicates that an existing property should be removed.") @RequestParam final Map<String, String> properties) {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        cleanProperties(properties);
        logConfigurationSubmit(host, port, username, password, protocol, properties);

        setHost(host, true);
        setPort(port);
        setProtocol(protocol);
        setUsername(username);
        setPassword(password);

        // Set all of the submitted properties.
        final Properties javaMailProperties = new Properties();
        if (properties != null) {
            for (final String property : properties.keySet()) {
                final String value = properties.get(property);
                if (StringUtils.isNotBlank(value)) {
                    javaMailProperties.setProperty(property, value);
                }
            }
        }

        // Find any existing properties that weren't submitted...
        final Properties existing = _javaMailSender.getJavaMailProperties();
        for (final String property : existing.stringPropertyNames()) {
            if (!javaMailProperties.containsKey(property)) {
                // Set the value to "", this will remove the property.
                javaMailProperties.setProperty(property, "");
            }
        }
        _javaMailSender.setJavaMailProperties(javaMailProperties);

        setSmtp();

        return getSmtpServerProperties();
    }

    @ApiOperation(value = "Sets the submitted mail service properties. This returns the SMTP server configuration as it exists after being set.", notes = PUT_PROPERTIES_NOTES, response = Properties.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Mail service properties successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the mail service properties."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"smtp"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.PUT})
    public ResponseEntity<Properties> setSubmittedMailProperties(@ApiParam("The value to set for the email host.") @RequestParam(value = "host", required = false, defaultValue = NOT_SET) final String host,
                                                                 @ApiParam("The value to set for the email port.") @RequestParam(value = "port", required = false, defaultValue = "-1") final int port,
                                                                 @ApiParam("The value to set for the email username.") @RequestParam(value = "username", required = false, defaultValue = NOT_SET) final String username,
                                                                 @ApiParam("The value to set for the email password.") @RequestParam(value = "password", required = false, defaultValue = NOT_SET) final String password,
                                                                 @ApiParam("The value to set for the email protocol.") @RequestParam(value = "protocol", required = false, defaultValue = NOT_SET) final String protocol,
                                                                 @ApiParam(value = "Values to set for extra mail properties. An empty value indicates that an existing property should be removed.") @RequestParam final Map<String, String> properties) {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        logConfigurationSubmit(host, port, username, password, protocol, properties);

        setHost(host, false);
        setPort(port);
        setProtocol(protocol);
        setUsername(username);
        setPassword(password);
        if (properties != null) {
            for (final String property : properties.keySet()) {
                _javaMailSender.getJavaMailProperties().setProperty(property, properties.get(property));
            }
        }

        setSmtp();

        return getSmtpServerProperties();
    }

    @ApiOperation(value = "Sets the mail service host.", notes = "Sets the mail service host.", response = Properties.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Mail service host successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the mail service host."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"smtp/host/{host}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.PUT})
    public ResponseEntity<Properties> setHostProperty(@ApiParam(value = "The value to set for the email host.", required = true) @PathVariable final String host) {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        if (_log.isInfoEnabled()) {
            _log.info("User " + getSessionUser().getLogin() + " setting mail host to: " + host);
        }
        setHost(host, true);
        setSmtp();
        return getSmtpServerProperties();
    }

    @ApiOperation(value = "Sets the mail service port.", notes = "Sets the mail service port.", response = Properties.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Mail service port successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the mail service port."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"smtp/port/{port}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.PUT})
    public ResponseEntity<Properties> setPortProperty(@ApiParam(value = "The value to set for the email port.", required = true) @PathVariable final int port) {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        if (_log.isInfoEnabled()) {
            _log.info("User " + getSessionUser().getLogin() + " setting mail port to: " + port);
        }
        setPort(port);
        setSmtp();
        return getSmtpServerProperties();
    }

    @ApiOperation(value = "Sets the mail service protocol.", notes = "Sets the mail service protocol.", response = Properties.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Mail service protocol successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the mail service protocol."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"smtp/protocol/{protocol}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.PUT})
    public ResponseEntity<Properties> setProtocolProperty(@ApiParam(value = "The value to set for the email protocol.", required = true) @PathVariable final String protocol) {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        if (_log.isInfoEnabled()) {
            _log.info("User " + getSessionUser().getLogin() + " setting mail protocol to: " + protocol);
        }
        setProtocol(protocol);
        return getSmtpServerProperties();
    }

    @ApiOperation(value = "Sets the mail service username.", notes = "Sets the mail service username.", response = Properties.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Mail service username successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the mail service username."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"smtp/username/{username}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.PUT})
    public ResponseEntity<Properties> setUsernameProperty(@ApiParam(value = "The value to set for the email username.", required = true) @PathVariable final String username) {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        if (_log.isInfoEnabled()) {
            _log.info("User " + getSessionUser().getLogin() + " setting mail username to: " + username);
        }
        setUsername(username);
        return getSmtpServerProperties();
    }

    @ApiOperation(value = "Sets the mail service password.", notes = "Sets the mail service password.", response = Properties.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Mail service password successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the mail service password."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"smtp/password/{password}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.PUT})
    public ResponseEntity<Properties> setPasswordProperty(@ApiParam(value = "The value to set for the email password.", required = true) @PathVariable final String password) {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        if (_log.isInfoEnabled()) {
            _log.info("User " + getSessionUser().getLogin() + " setting mail password to: ********");
        }
        setPassword(password);
        return getSmtpServerProperties();
    }

    @ApiOperation(value = "Sets a Java mail property with the submitted name and value.", notes = "Setting a property to an empty value will remove the property.", response = Properties.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Mail service password successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the mail service password."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"smtp/property/{property}/{value}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.PUT})
    public ResponseEntity<Properties> setExtendedProperty(@ApiParam(value = "The value to set for the email password.", required = true) @PathVariable final String property,
                                                          @ApiParam(value = "The value to set for the email password.", required = true) @PathVariable final String value) {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        if (_log.isInfoEnabled()) {
            _log.info("User " + getSessionUser().getLogin() + " setting mail password to: ********");
        }
        setProperty(property, value);
        return getSmtpServerProperties();
    }

    @ApiOperation(value = "Sets the email message for contacting help.", notes = "Sets the email message that people should receive when contacting help.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Help email message successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the help email message."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"messages/help"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST})
    public ResponseEntity<Void> setHelpContactInfo(@ApiParam(value = "The email message for contacting help.", required = true) @RequestParam final String message) {
        _siteConfigPrefs.setHelpContactInfo(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Sets the email message for user registration.", notes = "Sets the email message that people should receive when they register. Link for email validation is auto-populated.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User registration email message successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the user registration email message."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"messages/registration"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST})
    public ResponseEntity<Properties> setEmailMessageUserRegistration(@ApiParam(value = "The email message for user registration.", required = true) @RequestParam final String message) {
        _siteConfigPrefs.setEmailMessageUserRegistration(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Sets the email message for forgot username.", notes = "Sets the email message that people should receive when they click that they forgot their username.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Forgot username email message successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the forgot username email message."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"messages/forgotusername"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST})
    public ResponseEntity<Properties> setEmailMessageForgotUsernameRequest(@ApiParam(value = "The email message for forgot username.", required = true) @RequestParam final String message) {
        _siteConfigPrefs.setEmailMessageForgotUsernameRequest(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Sets the email message for password reset.", notes = "Sets the email message that people should receive when they click to reset their password.  Link for password reset is auto-populated.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Password reset message successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the password reset message."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"messages/passwordreset"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST})
    public ResponseEntity<Properties> setEmailMessageForgotPasswordReset(@ApiParam(value = "The email message for password reset.", required = true) @RequestParam final String message) {
        _siteConfigPrefs.setEmailMessageForgotPasswordReset(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Returns the email message for contacting help.", notes = "This returns the email message that people should receive when contacting help.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Email message for contacting help successfully returned."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to get email message for contacting help."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"messages/help"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<String> getHelpContactInfo() {
        return new ResponseEntity<>(_siteConfigPrefs.getHelpContactInfo(), HttpStatus.OK);
    }

    @ApiOperation(value = "Returns the email message for user registration.", notes = "This returns the email message that people should receive when they register. Link for email validation is auto-populated.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Email message for user registration successfully returned."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to get email message for user registration."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"messages/registration"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<String> getEmailMessageUserRegistration() {
        return new ResponseEntity<>(_siteConfigPrefs.getEmailMessageUserRegistration(), HttpStatus.OK);
    }

    @ApiOperation(value = "Returns the email message for forgot username.", notes = "This returns the email message that people should receive when they click that they forgot their username.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Email message for forgot username successfully returned."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to get email message for forgot username."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"messages/forgotusername"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<String> getEmailMessageForgotUsernameRequest() {
        return new ResponseEntity<>(_siteConfigPrefs.getEmailMessageForgotUsernameRequest(), HttpStatus.OK);
    }

    @ApiOperation(value = "Returns the email message for password reset.", notes = "This returns the email message that people should receive when they click to reset their password.  Link for password reset is auto-populated.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Email message for password reset successfully returned."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to get email message for password reset."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"messages/passwordreset"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<String> getEmailMessageForgotPasswordReset() {
        return new ResponseEntity<>(_siteConfigPrefs.getEmailMessageForgotPasswordReset(), HttpStatus.OK);
    }

    @ApiOperation(value = "Sets whether admins should be notified of user registration.", notes = "Sets whether admins should be notified of user registration.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Whether admins should be notified of user registration successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set whether admins should be notified of user registration."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"notify/registration"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST})
    public ResponseEntity<Void> setNotifyAdminUserRegistration(@ApiParam(value = "Whether admins should be notified of user registration successfully set.", required = true) @RequestParam final boolean notify) {
        _siteConfigPrefs.setNotifyAdminUserRegistration(notify);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Sets whether admins should be notified of pipeline processing submit.", notes = "Sets whether admins should be notified of pipeline processing submit.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Whether admins should be notified of pipeline processing submit successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set whether admins should be notified of pipeline processing submit."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"notify/pipeline"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST})
    public ResponseEntity<Properties> setNotifyAdminPipelineEmails(@ApiParam(value = "Whether admins should be notified of pipeline processing submit successfully set.", required = true) @RequestParam final boolean notify) {
        _siteConfigPrefs.setNotifyAdminPipelineEmails(notify);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Sets whether admins should be notified of project access requests.", notes = "Sets whether admins should be notified of project access requests.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Whether admins should be notified of project access requests successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set whether admins should be notified of project access requests."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"notify/par"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST})
    public ResponseEntity<Properties> setNotifyAdminProjectAccessRequest(@ApiParam(value = "Whether admins should be notified of project access requests successfully set.", required = true) @RequestParam final boolean notify) {
        _siteConfigPrefs.setNotifyAdminProjectAccessRequest(notify);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Sets whether admins should be notified of session transfer.", notes = "Sets whether admins should be notified of session transfer by user.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Whether admins should be notified of session transfer successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set whether admins should be notified of session transfer."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"notify/transfer"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST})
    public ResponseEntity<Properties> setNotifyAdminSessionTransfer(@ApiParam(value = "Whether admins should be notified of session transfer successfully set.", required = true) @RequestParam final boolean notify) {
        _siteConfigPrefs.setNotifyAdminSessionTransfer(notify);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Returns whether admins should be notified of user registration.", notes = "This returns whether admins should be notified of user registration.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Email message for contacting help successfully returned."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to get email message for contacting help."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"notify/registration"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<Boolean> getNotifyAdminUserRegistration() {
        return new ResponseEntity<>(_siteConfigPrefs.getNotifyAdminUserRegistration(), HttpStatus.OK);
    }

    @ApiOperation(value = "Returns whether admins should be notified of pipeline processing submit.", notes = "This returns whether admins should be notified of pipeline processing submit.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Email message for user registration successfully returned."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to get email message for user registration."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"notify/pipeline"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<Boolean> getNotifyAdminPipelineEmails() {
        return new ResponseEntity<>(_siteConfigPrefs.getNotifyAdminPipelineEmails(), HttpStatus.OK);
    }

    @ApiOperation(value = "Returns whether admins should be notified of project access requests.", notes = "This returns whether admins should be notified of project access requests.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Email message for forgot username successfully returned."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to get email message for forgot username."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"notify/par"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<Boolean> getNotifyAdminProjectAccessRequest() {
        return new ResponseEntity<>(_siteConfigPrefs.getNotifyAdminProjectAccessRequest(), HttpStatus.OK);
    }

    @ApiOperation(value = "Returns whether admins should be notified of session transfer.", notes = "This returns whether admins should be notified of session transfer.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Email message for password reset successfully returned."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to get email message for password reset."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"notify/transfer"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<Boolean> getNotifyAdminSessionTransfer() {
        return new ResponseEntity<>(_siteConfigPrefs.getNotifyAdminSessionTransfer(), HttpStatus.OK);
    }

    @ApiOperation(value = "Sets whether non-users should be able to subscribe to notifications.", notes = "Sets whether non-users should be able to subscribe to notifications.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Whether non-users should be able to subscribe to notifications."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set whether non-users should be able to subscribe to notifications."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"allow/nonusersubscribers/{setting}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST})
    public ResponseEntity<Properties> setEmailAllowNonuserSubscribers(@ApiParam(value = "Whether non-users should be able to subscribe to notifications.", required = true) @PathVariable final boolean setting) {
        _siteConfigPrefs.setEmailAllowNonuserSubscribers(setting);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Returns whether non-users should be able to subscribe to notifications.", notes = "This returns whether non-users should be able to subscribe to notifications.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Whether non-users should be able to subscribe to notifications successfully returned."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to get whether non-users should be able to subscribe to notifications."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"allow/nonusersubscribers"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<Boolean> getEmailAllowNonuserSubscribers() {
        return new ResponseEntity<>(_siteConfigPrefs.getEmailAllowNonuserSubscribers(), HttpStatus.OK);
    }

    @ApiOperation(value = "Sets the email addresses for error notifications.", notes = "Sets the email addresses that should be subscribed to error notifications.", response = Properties.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Error subscribers successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the error subscribers."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"subscribers/error"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST})
    public ResponseEntity<Void> setErrorSubscribers(@ApiParam(value = "The values to set for email addresses for error notifications.", required = true) @RequestParam final String subscribers) {
        setSubscribersForNotificationType(NotificationType.Error, subscribers);
        _siteConfigPrefs.setEmailRecipientErrorMessages(subscribers);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Sets the email addresses for issue notifications.", notes = "Sets the email addresses that should be subscribed to issue notifications.", response = Properties.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Issue subscribers successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the issue subscribers."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"subscribers/issue"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST})
    public ResponseEntity<Properties> setIssueSubscribers(@ApiParam(value = "The values to set for email addresses for issue notifications.", required = true) @RequestParam final String subscribers) {
        setSubscribersForNotificationType(NotificationType.Issue, subscribers);
        _siteConfigPrefs.setEmailRecipientIssueReports(subscribers);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Sets the email addresses for new user notifications.", notes = "Sets the email addresses that should be subscribed to new user notifications.", response = Properties.class)
    @ApiResponses({@ApiResponse(code = 200, message = "New user subscribers successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the new user subscribers."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"subscribers/newuser"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST})
    public ResponseEntity<Properties> setNewUserSubscribers(@ApiParam(value = "The values to set for email addresses for new user notifications.", required = true) @RequestParam final String subscribers) {
        setSubscribersForNotificationType(NotificationType.NewUser, subscribers);
        _siteConfigPrefs.setEmailRecipientNewUserAlert(subscribers);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Sets the email addresses for update notifications.", notes = "Sets the email addresses that should be subscribed to update notifications.", response = Properties.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Update subscribers successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the update subscribers."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"subscribers/update"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST})
    public ResponseEntity<Properties> setUpdateSubscribers(@ApiParam(value = "The values to set for email addresses for update notifications.", required = true) @RequestParam final String subscribers) {
        setSubscribersForNotificationType(NotificationType.Update, subscribers);
        _siteConfigPrefs.setEmailRecipientUpdate(subscribers);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Returns list of email addresses subscribed to error notifications.", notes = "This returns a list of all the email addresses that are subscribed to receive error notifications.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Error notification subscribers successfully returned."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to get subscribers for email notifications."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"subscribers/error"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<String> getErrorSubscribers() {
        return new ResponseEntity<>(_siteConfigPrefs.getEmailRecipientErrorMessages(), HttpStatus.OK);
    }

    @ApiOperation(value = "Returns list of email addresses subscribed to issue notifications.", notes = "This returns a list of all the email addresses that are subscribed to receive issue notifications.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Issue notification subscribers successfully returned."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to get subscribers for email notifications."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"subscribers/issue"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<String> getIssueSubscribers() {
        return new ResponseEntity<>(_siteConfigPrefs.getEmailRecipientIssueReports(), HttpStatus.OK);
    }

    @ApiOperation(value = "Returns list of email addresses subscribed to new user notifications.", notes = "This returns a list of all the email addresses that are subscribed to receive new user notifications.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "New user notification subscribers successfully returned."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to get subscribers for email notifications."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"subscribers/newuser"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<String> getNewUserSubscribers() {
        return new ResponseEntity<>(_siteConfigPrefs.getEmailRecipientNewUserAlert(), HttpStatus.OK);
    }

    @ApiOperation(value = "Returns list of email addresses subscribed to update notifications.", notes = "This returns a list of all the email addresses that are subscribed to receive update notifications.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Update notification subscribers successfully returned."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to get subscribers for email notifications."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"subscribers/update"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<String> getUpdateSubscribers() {
        return new ResponseEntity<>(_siteConfigPrefs.getEmailRecipientUpdate(), HttpStatus.OK);
    }

    private void setSmtp() {
        Map<String, String> smtp = new HashMap<>();
        smtp.put("host", StringUtils.defaultIfBlank(_javaMailSender.getHost(), "localhost"));
        smtp.put("port", Integer.toString(_javaMailSender.getPort()));
        if (StringUtils.isNotBlank(_javaMailSender.getUsername())) {
            smtp.put("username", _javaMailSender.getUsername());
        }
        if (StringUtils.isNotBlank(_javaMailSender.getPassword())) {
            smtp.put("password", _javaMailSender.getPassword());
        }
        if (StringUtils.isNotBlank(_javaMailSender.getProtocol())) {
            smtp.put("protocol", _javaMailSender.getProtocol());
        }
        final Properties properties = _javaMailSender.getJavaMailProperties();
        if (properties.size() > 0) {
            for (final String property : properties.stringPropertyNames()) {
                smtp.put(property, properties.getProperty(property));
            }
        }
        _siteConfigPrefs.setSmtpServer(smtp);
    }

    private void cleanProperties(final Map<String, String> properties) {
        if (properties.containsKey("host")) {
            properties.remove("host");
        }
        if (properties.containsKey("port")) {
            properties.remove("port");
        }
        if (properties.containsKey("protocol")) {
            properties.remove("protocol");
        }
        if (properties.containsKey("username")) {
            properties.remove("username");
        }
        if (properties.containsKey("password")) {
            properties.remove("password");
        }
    }

    private void setHost(final String host, final boolean freakOutIfBlank) {
        if (freakOutIfBlank && StringUtils.isBlank(host)) {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "You can not set the SMTP server address to an empty value!");
        }
        if (StringUtils.isNotBlank(host)) {
            _javaMailSender.setHost(host);
        }
    }

    private void setPort(final int port) {
        if (port != -1) {
            _javaMailSender.setPort(port);
        }
    }

    private void setProtocol(final String protocol) {
        if (!StringUtils.equals(NOT_SET, protocol)) {
            _javaMailSender.setProtocol(protocol);
        }
    }

    private void setUsername(final String username) {
        if (!StringUtils.equals(NOT_SET, username)) {
            _javaMailSender.setUsername(username);
        }
    }

    private void setPassword(final String password) {
        if (!StringUtils.equals(NOT_SET, password)) {
            _javaMailSender.setPassword(password);
        }
    }

    private void setProperty(final String property, final String value) {
        final Properties properties = _javaMailSender.getJavaMailProperties();
        if (properties.containsKey(property) && StringUtils.isBlank(value)) {
            properties.remove(property);
        } else {
            properties.setProperty(property, value);
        }
    }

    private void logConfigurationSubmit(final String host, final int port, final String username, final String password, final String protocol, final Map<String, String> properties) {
        if (_log.isInfoEnabled()) {
            final StringBuilder message = new StringBuilder("User ");
            message.append(getSessionUser().getLogin()).append(" setting mail properties to:\n");
            message.append(" * Host:     ").append(StringUtils.equals(NOT_SET, host) ? "No value submitted..." : host).append("\n");
            message.append(" * Port:     ").append(port == -1 ? "No value submitted..." : port).append("\n");
            message.append(" * Protocol: ").append(StringUtils.equals(NOT_SET, protocol) ? "No value submitted..." : protocol).append("\n");
            message.append(" * Username: ").append(StringUtils.equals(NOT_SET, username) ? "No value submitted..." : username).append("\n");
            message.append(" * Password: ").append(StringUtils.equals(NOT_SET, password) ? "No value submitted..." : "********").append("\n");
            if (properties != null && properties.size() > 0) {
                for (final String property : properties.keySet()) {
                    message.append(" * ").append(property).append(": ").append(properties.get(property)).append("\n");
                }
            }
            _log.info(message.toString());
        }
    }

    private void setSubscribersForNotificationType(NotificationType notificationType, final String subscribersString){
        List<String> subscribers = Arrays.asList(subscribersString.split("\\s*,\\s*"));
        Category category = _notificationService.getCategoryService().newEntity();
        category.setScope(CategoryScope.Site);
        category.setEvent(notificationType.id());
        for(String subscriber : subscribers){
            try {
                Subscriber subscriberObject = _notificationService.getSubscriberService().getSubscriberByName(subscriber);

                if(subscriberObject==null){
                    subscriberObject = _notificationService.getSubscriberService().createSubscriber(subscriber, subscriber);
                }

                Definition definition1 = _notificationService.getDefinitionService().getDefinitionForCategoryAndEntity(category,1L);
                if(definition1==null) {
                    definition1 = _notificationService.createDefinition(CategoryScope.Site, notificationType.id(), 1L);
                }

                Channel channel1 = _notificationService.getChannelService().getChannel("htmlMail");
                if(channel1==null) {
                    _notificationService.getChannelService().createChannel("htmlMail", "text/html");
                }

                Map<Subscriber, Subscription> subscriberMapOfSubscriptions = _notificationService.getSubscriptionService().getSubscriberMapOfSubscriptionsForDefinition(definition1);
                for (Map.Entry<Subscriber, Subscription> entry : subscriberMapOfSubscriptions.entrySet()) {
                    //Remove all existing subscriptions that match this definition since we are replacing the old list with the new one.
                    Subscriber tempSubscriber = entry.getKey();
                    Subscription tempSubscription = entry.getValue();
                    tempSubscriber.removeSubscription(tempSubscription);
                }
                Subscription subscription = _notificationService.subscribe(subscriberObject, SubscriberType.User, definition1, channel1);

            } catch (DuplicateSubscriberException e) {
                _log.error("You tried to subscribe someone who was already subscribed",e);
            } catch (DuplicateDefinitionException e) {
                _log.error("Multiple definitions for this scope, event, and entity exist.",e);
            } catch (NrgServiceException e) {
                _log.error("Error setting email addresses for error notifications.",e);
            }
        }
    }

//    private List<String> getSubscribersForNotificationType(NotificationType notificationType){
//        List<String> subscriberEmails = new ArrayList<String>();
//        Category category = _notificationService.getCategoryService().newEntity();
//        category.setScope(CategoryScope.Site);
//        category.setEvent(notificationType.id());
//        Definition definition1 = null;
//        try {
//            definition1 = _notificationService.getDefinitionService().getDefinitionForCategoryAndEntity(category,1L);
//            List<Subscription> subscriptions = definition1.getSubscriptions();
//            for(Subscription subscription : subscriptions){
//                for(String email : subscription.getSubscriber().getEmailList()){
//                    subscriberEmails.add(email);
//                }
//            }
//        } catch (DuplicateDefinitionException e) {
//            _log.error("Multiple definitions for this scope, event, and entity exist.",e);
//        }
//        return subscriberEmails;
//    }

    private static final Logger _log    = LoggerFactory.getLogger(NotificationsApi.class);
    private static final String NOT_SET = "NotSet";

    @Inject
    private SiteConfigPreferences _siteConfigPrefs;

    @Inject
    private JavaMailSenderImpl _javaMailSender;

    @Inject
    private NotificationService _notificationService;
}
