/*
 * web: org.nrg.xapi.model.users.LoginRecord
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *  
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.model.users;

import org.nrg.xdat.entities.XdatUserAuth;

import java.util.Date;

/**
 * Represents login attributes for a {@link User}: authentication method, date the password was last updated, current
 * number of failed login attempts, date of the latest login attempt, date of the latest successful login, and lockout
 * time.
 */
public class LoginRecord {
    LoginRecord() {
        _authMethodId = null;
        _timestamp = null;
        _passwordUpdated = null;
        _failedLoginAttempts = null;
        _lastLoginAttempt = null;
        _lastSuccessfulLogin = null;
        _lockoutTime = null;
    }

    LoginRecord(final XdatUserAuth auth) {
        _authMethodId = auth.getAuthMethodId();
        _timestamp = auth.getTimestamp();
        _passwordUpdated = auth.getPasswordUpdated();
        _failedLoginAttempts = auth.getFailedLoginAttempts();
        _lastLoginAttempt = auth.getLastLoginAttempt();
        _lastSuccessfulLogin = auth.getLastSuccessfulLogin();
        _lockoutTime = auth.getLockoutTime();
    }

    public String getAuthMethodId() {
        return _authMethodId;
    }

    public Date getTimestamp() {
        return _timestamp;
    }

    public Date getPasswordUpdated() {
        return _passwordUpdated;
    }

    public Integer getFailedLoginAttempts() {
        return _failedLoginAttempts;
    }

    public Date getLastLoginAttempt() {
        return _lastLoginAttempt;
    }

    public Date getLastSuccessfulLogin() {
        return _lastSuccessfulLogin;
    }

    public Date getLockoutTime() {
        return _lockoutTime;
    }

    private final String  _authMethodId;
    private final Date    _timestamp;
    private final Date    _passwordUpdated;
    private final Integer _failedLoginAttempts;
    private final Date    _lastLoginAttempt;
    private final Date    _lastSuccessfulLogin;
    private final Date    _lockoutTime;
}
