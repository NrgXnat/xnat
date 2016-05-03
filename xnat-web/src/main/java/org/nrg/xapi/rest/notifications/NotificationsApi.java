package org.nrg.xapi.rest.notifications;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.rest.AbstractXnatRestApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
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
    private static final Logger _log = LoggerFactory.getLogger(NotificationsApi.class);

    @ApiOperation(value = "Sets all the mail service properties.", notes = "Sets the mail service host, port, username, password, and protocol.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Mail service properties successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the mail service properties."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/all"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST})
    public ResponseEntity<Void> setMailProperties(@ApiParam(value = "The value to set for the email host.") @RequestParam(value = "host", required = false) final String host,
                                                  @ApiParam(value = "The value to set for the email port.") @RequestParam(value = "port", required = false) final int port,
                                                  @ApiParam(value = "The value to set for the email username.") @RequestParam(value = "username", required = false) final String username,
                                                  @ApiParam(value = "The value to set for the email password.") @RequestParam(value = "password", required = false) final String password,
                                                  @ApiParam(value = "The value to set for the email protocol.") @RequestParam(value = "protocol", required = false) final String protocol,
                                                  @ApiParam(value = "Values to set for extra mail properties.") @RequestParam(value = "properties", required = false) final Map<String, String> properties) {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        if (_log.isInfoEnabled()) {
            final StringBuilder message = new StringBuilder("User ");
            message.append(getSessionUser().getLogin()).append(" setting mail properties to:\n");
            if (StringUtils.isNotBlank(host)) {
                message.append(" * Host:     ").append(host).append("\n");
            }
            if (port != 0) {
                message.append(" * Port:     ").append(port).append("\n");
            }
            if (StringUtils.isNotBlank(username)) {
                message.append(" * Username: ").append(username).append("\n");
            }
            if (StringUtils.isNotBlank(password)) {
                message.append(" * Password: ********\n");
            }
            if (StringUtils.isNotBlank(protocol)) {
                message.append(" * Protocol: ").append(protocol).append("\n");
            }
            if (properties != null && properties.size() > 0) {
                for (final String property : properties.keySet()) {
                    message.append(" * ").append(property).append(": ").append(properties.get(property)).append("\n");
                }
            }
            _log.info(message.toString());
        }
        _javaMailSender.setHost(host);
        _javaMailSender.setPort(port);
        _javaMailSender.setUsername(username);
        _javaMailSender.setPassword(password);
        _javaMailSender.setProtocol(protocol);
        if (properties != null) {
            final Properties javaMailProperties = new Properties();
            javaMailProperties.putAll(properties);
            _javaMailSender.setJavaMailProperties(javaMailProperties);
        }
        setSmtp();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Sets the mail service host.", notes = "Sets the mail service host.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Mail service host successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the mail service host."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/host"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST})
    public ResponseEntity<Void> setHostProperty(@ApiParam(value = "The value to set for the email host.", required = true) @RequestParam("host") final String host) {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        if (_log.isInfoEnabled()) {
            _log.info("User " + getSessionUser().getLogin() + " setting mail host to: " + host);
        }
        _javaMailSender.setHost(host);
        setSmtp();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Sets the mail service port.", notes = "Sets the mail service port.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Mail service port successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the mail service port."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/port"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST})
    public ResponseEntity<Void> setPortProperty(@ApiParam(value = "The value to set for the email port.", required = true) @RequestParam("port") final int port) {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        if (_log.isInfoEnabled()) {
            _log.info("User " + getSessionUser().getLogin() + " setting mail port to: " + port);
        }
        _javaMailSender.setPort(port);
        setSmtp();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Sets the mail service protocol.", notes = "Sets the mail service protocol.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Mail service protocol successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the mail service protocol."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/protocol"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST})
    public ResponseEntity<Void> setProtocolProperty(@ApiParam(value = "The value to set for the email protocol.", required = true) @RequestParam("protocol") final String protocol) {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        if (_log.isInfoEnabled()) {
            _log.info("User " + getSessionUser().getLogin() + " setting mail protocol to: " + protocol);
        }
        _javaMailSender.setProtocol(protocol);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Sets the mail service username.", notes = "Sets the mail service username.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Mail service username successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the mail service username."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/username"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST})
    public ResponseEntity<Void> setUsernameProperty(@ApiParam(value = "The value to set for the email username.", required = true) @RequestParam("username") final String username) {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        if (_log.isInfoEnabled()) {
            _log.info("User " + getSessionUser().getLogin() + " setting mail username to: " + username);
        }
        _javaMailSender.setUsername(username);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Sets the mail service password.", notes = "Sets the mail service password.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Mail service password successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the mail service password."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/password"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.POST})
    public ResponseEntity<Void> setPasswordProperty(@ApiParam(value = "The value to set for the email password.", required = true) @RequestParam("password") final String password) {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        if (_log.isInfoEnabled()) {
            _log.info("User " + getSessionUser().getLogin() + " setting mail password to: ********");
        }
        _javaMailSender.setPassword(password);
        return new ResponseEntity<>(HttpStatus.OK);
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

    @Inject
    private SiteConfigPreferences _siteConfigPrefs;

    @Inject
    private JavaMailSenderImpl _javaMailSender;
}
