/*
 * web: org.nrg.xapi.model.users.UserAuth
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.model.users;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiModel(description = "Contains the properties that define a user's authentication provider mapping entry on the system.")
@Data
@Accessors(prefix = "_")
@Slf4j
@JsonPropertyOrder({"xdatUsername", "authUser", "authMethod", "authMethodId", "lastLoginAttempt", "lastSuccessfulLogin", "failedLoginAttempts", "lockoutTime", "passwordUpdated", "authorities"})
public class UserAuth {
    public void resetFailedLogins() {
        setFailedLoginAttempts(0);
        setLockoutTime(null);
    }

    @Override
    public String toString() {
        return "class UserAuth {\n"
               + "  xdatUsername: " + getXdatUsername() + "\n"
               + "  authUser: " + getAuthUser() + "\n"
               + "  authMethod: " + getAuthMethod() + "\n"
               + "  authMethodId: " + getAuthMethodId() + "\n"
               + "  failedLoginAttempts: " + getFailedLoginAttempts() + "\n"
               + "  lastSuccessfulLogin: " + getLastSuccessfulLogin() + "\n"
               + "  passwordUpdated: " + getPasswordUpdated() + "\n"
               + "  authorities: " + StringUtils.join(getAuthorities(), ", ") + "\n"
               + "}\n";
    }

    private String       _xdatUsername        = null;
    private User         _authUser            = null;
    private String       _authMethod          = null;
    private String       _authMethodId        = null;
    private Date         _lastLoginAttempt    = null;
    private Date         _lastSuccessfulLogin = null;
    private Integer      _failedLoginAttempts = null;
    private Date         _lockoutTime         = null;
    private Date         _passwordUpdated     = null;
    private List<String> _authorities         = new ArrayList<>();
}
