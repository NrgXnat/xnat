/*
 * web: org.nrg.xapi.model.users.User
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.model.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.nrg.xdat.entities.UserAuthI;

import java.util.Date;
import java.util.Map;

/**
 * A transport container for user details. The {@link #isSecured() secured property} controls whether security-related
 * properties like password and salt are available. When a new user object is created from an existing user record in
 * XNAT, the secure flag is set to true. This prevents serializing beans with existing user accounts to prevent exposing
 * password data. Newly created beans have secure set to false by default to allow for serializing the bean for REST
 * calls with all data intact.
 */
@ApiModel(description = "Contains the properties that define a user on the system.")
public class User {
    /**
     * The user's unique key.
     **/
    @ApiModelProperty(value = "The user's unique key.")
    public Integer getId() {
        return _id;
    }

    public void setId(final Integer id) {
        _id = id;
    }

    /**
     * The user's login name.
     **/
    @ApiModelProperty(value = "The user's login name.")
    public String getUsername() {
        return _username;
    }

    public void setUsername(final String username) {
        _username = username;
    }

    /**
     * The user's first name.
     **/
    @ApiModelProperty(value = "The user's first name.")
    public String getFirstName() {
        return _firstName;
    }

    @SuppressWarnings("unused")
    public void setFirstName(final String firstName) {
        _firstName = firstName;
    }

    /**
     * The user's last name.
     **/
    @ApiModelProperty(value = "The user's last name.")
    public String getLastName() {
        return _lastName;
    }

    @SuppressWarnings("unused")
    public void setLastName(final String lastName) {
        _lastName = lastName;
    }

    /**
     * The user's _email address.
     **/
    @ApiModelProperty(value = "The user's email address.")
    public String getEmail() {
        return _email;
    }

    public void setEmail(final String email) {
        _email = email;
    }

    /**
     * Whether the user is a site administrator.
     **/
    @ApiModelProperty(value = "Whether the user is a site administrator.")
    public Boolean isAdmin() {
        return _secured ? null : _isAdmin;
    }

    public void setAdmin(final boolean isAdmin) {
        _isAdmin = isAdmin;
    }

    /**
     * Whether the user is enabled.
     **/
    @ApiModelProperty(value = "Whether the user is enabled.")
    public Boolean isEnabled() {
        return _isEnabled;
    }

    public void setEnabled(final boolean isEnabled) {
        _isEnabled = isEnabled;
    }

    /**
     * Whether the user is verified.
     **/
    @ApiModelProperty(value = "Whether the user is verified.")
    public Boolean isVerified() {
        return _isVerified;
    }

    public void setVerified(final boolean isVerified) {
        _isVerified = isVerified;
    }

    /**
     * The user's encrypted password.
     **/
    @ApiModelProperty(value = "The user's encrypted password.")
    public String getPassword() {
        return _secured ? null : _password;
    }

    public void setPassword(final String password) {
        _password = password;
    }

    /**
     * The _salt used to encrypt the user's _password.
     **/
    @ApiModelProperty(value = "The salt used to encrypt the user's password.")
    public String getSalt() {
        return _secured ? null : _salt;
    }

    @SuppressWarnings("unused")
    public void setSalt(final String salt) {
        _salt = salt;
    }

    /**
     * The date and time the user record was last modified.
     **/
    @ApiModelProperty(value = "The date and time the user record was last modified.")
    public Date getLastModified() {
        return _lastModified;
    }

    public void setLastModified(final Date lastModified) {
        _lastModified = lastModified;
    }

    /**
     * The user's authorization record used when logging in.
     **/
    @ApiModelProperty(value = "The user's authorization record used when logging in.")
    public UserAuthI getAuthorization() {
        return _secured ? null : _authorization;
    }

    @SuppressWarnings("unused")
    public void setAuthorization(final UserAuthI authorization) {
        _authorization = authorization;
    }

    /**
     * Returns the authentication method ID of the most recently used authentication provider.
     *
     * @return The ID of the most recently used authentication provider.
     */
    @ApiModelProperty("Returns ID of the most recently used authentication provider.")
    public String getAuthMethodId() {
        return getLatestLoginRecord().getAuthMethodId();
    }

    /**
     * Returns the date and time that the password was updated for the most recently used authentication provider.
     *
     * @return The date and time that the password was updated for the most recently used authentication provider.
     */
    @ApiModelProperty("Returns the date and time that the password was updated for the most recently used authentication provider.")
    public Date getPasswordUpdated() {
        return getLatestLoginRecord().getPasswordUpdated();
    }

    /**
     * Returns the number of failed login attempts for the most recently used authentication provider.
     *
     * @return The number of failed login attempts for the most recently used authentication provider.
     */
    @ApiModelProperty("Returns the number of failed login attempts for the most recently used authentication provider.")
    public Integer getFailedLoginAttempts() {
        return getLatestLoginRecord().getFailedLoginAttempts();
    }

    /**
     * Returns the date and time of the last login attempt for the most recently used authentication provider.
     *
     * @return The date and time of the last login attempt for the most recently used authentication provider.
     */
    @ApiModelProperty("The date and time of the last login attempt for the most recently used authentication provider.")
    public Date getLastLoginAttempt() {
        return getLatestLoginRecord().getLastLoginAttempt();
    }

    /**
     * Returns the date and time of the last successful login attempt for the most recently used authentication provider.
     *
     * @return The date and time of the last successful login attempt for the most recently used authentication provider.
     */
    @ApiModelProperty("The date and time of the last successful login attempt for the most recently used authentication provider.")
    public Date getLastSuccessfulLogin() {
        return getLatestLoginRecord().getLastSuccessfulLogin();
    }

    /**
     * Returns the date and time that the user was locked out of the most recently used authentication provider.
     *
     * @return The date and time that the user was locked out of the most recently used authentication provider.
     */
    @ApiModelProperty("The date and time that the user was locked out of the most recently used authentication provider.")
    public Date getLockoutTime() {
        return getLatestLoginRecord().getLockoutTime();
    }

    @ApiModelProperty(value = "The user's login records, which includes each available login method, the user's login"
                              + "name for that method, and data about logins using that method (failed login count, last"
                              + "successful login, and so on).")
    public Map<String, LoginRecord> getLoginRecords() {
        return ImmutableMap.copyOf(_loginRecords);
    }

    public void setLoginRecords(final Map<String, LoginRecord> loginRecords) {
        _loginRecords.clear();
        _loginRecords.putAll(loginRecords);
        _latest = null;
    }

    @ApiModelProperty(value = "The user's full name.")
    @JsonIgnore
    public String getFullName() {
        return String.format("%s %s", getFirstName(), getLastName());
    }

    @ApiModelProperty(value = "Indicates whether the user object is secured, which causes secure fields like password and salt to return null.")
    public boolean isSecured() {
        return _secured;
    }

    public void setSecured(final boolean secured) {
        _secured = secured;
    }

    @Override
    public String toString() {
        return "class User {\n" +
               "  id: " + _id + "\n" +
               "  username: " + _username + "\n" +
               "  firstName: " + _firstName + "\n" +
               "  lastName: " + _lastName + "\n" +
               "  email: " + _email + "\n" +
               "  dbName: " + _dbName + "\n" +
               "  password: " + _password + "\n" +
               "  salt: " + _salt + "\n" +
               "  lastModified: " + _lastModified + "\n" +
               "  authorization: " + _authorization + "\n" +
               "}\n";
    }

    private LoginRecord getLatestLoginRecord() {
        if (_latest == null) {
            for (final LoginRecord current : _loginRecords.values()) {
                if (_latest == null) {
                    _latest = current;
                } else if (_latest.getTimestamp().before(current.getTimestamp())) {
                    _latest = current;
                }
            }
        }
        return _latest == null ? EMPTY_RECORD : _latest;
    }

    private static final LoginRecord EMPTY_RECORD = new LoginRecord();

    private Integer     _id;
    private String      _username;
    private String      _firstName;
    private String      _lastName;
    private String      _email;
    private String      _dbName;
    private String      _password;
    private String      _salt;
    private boolean     _secured;
    private Date        _lastModified;
    private UserAuthI   _authorization;
    private Boolean     _isAdmin;
    private Boolean     _isEnabled;
    private Boolean     _isVerified;
    private LoginRecord _latest;

    private final Map<String, LoginRecord> _loginRecords = Maps.newHashMap();
}
