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

    }

    LoginRecord(final XdatUserAuth auth) {
        setAuthMethodId(auth.getAuthMethodId());
        setPasswordUpdated(auth.getPasswordUpdated());
        setFailedLoginAttempts(auth.getFailedLoginAttempts());
        setLastLoginAttempt(auth.getLastLoginAttempt());
        setLastSuccessfulLogin(auth.getLastSuccessfulLogin());
        setLockoutTime(auth.getLockoutTime());
    }

    LoginRecord(final String authMethodId,
                final Date passwordUpdated,
                final Integer failedLoginAttempts,
                final Date lastLoginAttempt,
                final Date lastSuccessfulLogin,
                final Date lockoutTime) {
        setAuthMethodId(authMethodId);
        setPasswordUpdated(passwordUpdated);
        setFailedLoginAttempts(failedLoginAttempts);
        setLastLoginAttempt(lastLoginAttempt);
        setLastSuccessfulLogin(lastSuccessfulLogin);
        setLockoutTime(lockoutTime);
    }

    public String getAuthMethodId() {
        return _authMethodId;
    }

    public void setAuthMethodId(final String authMethodId) {
        _authMethodId = authMethodId;
    }

    public Date getPasswordUpdated() {
        return _passwordUpdated;
    }

    public void setPasswordUpdated(final Date passwordUpdated) {
        _passwordUpdated = passwordUpdated;
    }

    public Integer getFailedLoginAttempts() {
        return _failedLoginAttempts;
    }

    public void setFailedLoginAttempts(final Integer failedLoginAttempts) {
        _failedLoginAttempts = failedLoginAttempts;
    }

    public Date getLastLoginAttempt() {
        return _lastLoginAttempt;
    }

    public void setLastLoginAttempt(final Date lastLoginAttempt) {
        _lastLoginAttempt = lastLoginAttempt;
    }

    public Date getLastSuccessfulLogin() {
        return _lastSuccessfulLogin;
    }

    public void setLastSuccessfulLogin(final Date lastSuccessfulLogin) {
        _lastSuccessfulLogin = lastSuccessfulLogin;
    }

    public Date getLockoutTime() {
        return _lockoutTime;
    }

    public void setLockoutTime(final Date lockoutTime) {
        _lockoutTime = lockoutTime;
    }

    private String  _authMethodId;
    private Date    _passwordUpdated;
    private Integer _failedLoginAttempts;
    private Date    _lastLoginAttempt;
    private Date    _lastSuccessfulLogin;
    private Date    _lockoutTime;
}
