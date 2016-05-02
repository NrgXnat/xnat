package org.nrg.xapi.rest.notifications;

import io.swagger.annotations.*;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.rest.AbstractXnatRestApi;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@Api(description = "XNAT Notifications management API")
@XapiRestController
@RequestMapping(value = "/notifications")
public class NotificationsApi extends AbstractXnatRestApi {
    private static final Logger _log = LoggerFactory.getLogger(NotificationsApi.class);

    @ApiOperation(value = "Sets all the mail service properties.", notes = "Sets the mail service host, port, username, password, and protocol.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Mail service properties successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the mail service properties."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/all"}, produces = {"application/json"}, method = {RequestMethod.POST})
    public ResponseEntity<Void> setMailProperties(@ApiParam(value = "The value to set for the email host.", required = true) @RequestParam(value="host", required=false) final String host,
                                                  @ApiParam(value = "The value to set for the email port.", required = true) @RequestParam(value="port", required=false) final int port,
                                                  @ApiParam(value = "The value to set for the email username.", required = true) @RequestParam(value="username", required=false) final String username,
                                                  @ApiParam(value = "The value to set for the email password.", required = true) @RequestParam(value="password", required=false) final String password,
                                                  @ApiParam(value = "The value to set for the email protocol.", required = true) @RequestParam(value="protocol", required=false) final String protocol) {
        HttpStatus status = null;
        UserI sessionUser = getSessionUser();
        if (sessionUser == null) {
            status = HttpStatus.UNAUTHORIZED;
        }
        else if ((sessionUser instanceof XDATUser)) {
            status = ((XDATUser) sessionUser).isSiteAdmin() ? null : HttpStatus.FORBIDDEN;
        }
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        _javaMailSender.setHost(host);
        _javaMailSender.setPort(port);
        _javaMailSender.setUsername(username);
        _javaMailSender.setPassword(password);
        _javaMailSender.setProtocol(protocol);
        setSmtp();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Sets the mail service host.", notes = "Sets the mail service host.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Mail service host successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the mail service host."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/host"}, produces = {"application/json"}, method = {RequestMethod.POST})
    public ResponseEntity<Void> setHostProperty(@ApiParam(value = "The value to set for the email host.", required = true) @RequestParam(value="host", required=true) final String host) {


        _javaMailSender.setHost(host);
        setSmtp();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Sets the mail service port.", notes = "Sets the mail service port.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Mail service port successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the mail service port."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/port"}, produces = {"application/json"}, method = {RequestMethod.POST})
    public ResponseEntity<Void> setPortProperty(@ApiParam(value = "The value to set for the email port.", required = true) @RequestParam(value="port", required=true) final int port) {
        HttpStatus status = null;
        UserI sessionUser = getSessionUser();
        if (sessionUser == null) {
            status = HttpStatus.UNAUTHORIZED;
        }
        else if ((sessionUser instanceof XDATUser)) {
            status = ((XDATUser) sessionUser).isSiteAdmin() ? null : HttpStatus.FORBIDDEN;
        }
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        _javaMailSender.setPort(port);
        setSmtp();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Sets the mail service protocol.", notes = "Sets the mail service protocol.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Mail service protocol successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the mail service protocol."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/protocol"}, produces = {"application/json"}, method = {RequestMethod.POST})
    public ResponseEntity<Void> setProtocolProperty(@ApiParam(value = "The value to set for the email protocol.", required = true) @RequestParam(value="protocol", required=true) final String protocol) {
        HttpStatus status = null;
        UserI sessionUser = getSessionUser();
        if (sessionUser == null) {
            status = HttpStatus.UNAUTHORIZED;
        }
        else if ((sessionUser instanceof XDATUser)) {
            status = ((XDATUser) sessionUser).isSiteAdmin() ? null : HttpStatus.FORBIDDEN;
        }
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        _javaMailSender.setProtocol(protocol);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Sets the mail service username.", notes = "Sets the mail service username.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Mail service username successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the mail service username."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/username"}, produces = {"application/json"}, method = {RequestMethod.POST})
    public ResponseEntity<Void> setUsernameProperty(@ApiParam(value = "The value to set for the email username.", required = true) @RequestParam(value="username", required=true) final String username) {
        HttpStatus status = null;
        UserI sessionUser = getSessionUser();
        if (sessionUser == null) {
            status = HttpStatus.UNAUTHORIZED;
        }
        else if ((sessionUser instanceof XDATUser)) {
            status = ((XDATUser) sessionUser).isSiteAdmin() ? null : HttpStatus.FORBIDDEN;
        }
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        _javaMailSender.setUsername(username);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Sets the mail service password.", notes = "Sets the mail service password.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Mail service password successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set the mail service password."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/password"}, produces = {"application/json"}, method = {RequestMethod.POST})
    public ResponseEntity<Void> setPasswordProperty(@ApiParam(value = "The value to set for the email password.", required = true) @RequestParam(value="password", required=true) final String password) {
        HttpStatus status = null;
        UserI sessionUser = getSessionUser();
        if (sessionUser == null) {
            status = HttpStatus.UNAUTHORIZED;
        }
        else if ((sessionUser instanceof XDATUser)) {
            status = ((XDATUser) sessionUser).isSiteAdmin() ? null : HttpStatus.FORBIDDEN;
        }
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        _javaMailSender.setPassword(password);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void setSmtp(){
        Map<String, String> smtp = new HashMap<String, String>();
        String host = _javaMailSender.getHost();
        if(host==null){
            host = "";
        }
        smtp.put("host", host);
        String port = ""+_javaMailSender.getPort();
        if(port==null){
            port = "";
        }
        smtp.put("port", port);
        _siteConfigPrefs.setSmtpServer(smtp);
    }


    @Inject
    private SiteConfigPreferences _siteConfigPrefs;

    @Inject
    private JavaMailSenderImpl _javaMailSender;
}
