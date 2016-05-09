package org.nrg.xapi.rest.notifications;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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

    private static final Logger _log    = LoggerFactory.getLogger(NotificationsApi.class);
    private static final String NOT_SET = "NotSet";

    @Inject
    private SiteConfigPreferences _siteConfigPrefs;

    @Inject
    private JavaMailSenderImpl _javaMailSender;
}
