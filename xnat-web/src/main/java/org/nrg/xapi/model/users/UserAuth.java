/*
 * web: org.nrg.xapi.model.users.UserAuth
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.model.users;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiModel(description = "Contains the properties that define a user's authentication provider mapping entry on the system.")
@Data
@Accessors(prefix = "_")
@NoArgsConstructor
@Slf4j
@JsonPropertyOrder({"xdatUsername", "authUser", "authMethod", "authMethodId", "lastLoginAttempt", "lastSuccessfulLogin", "failedLoginAttempts", "lockoutTime", "passwordUpdated", "authorities"})
public class UserAuth {
    public UserAuth(final String authMethod, final String authMethodId, final String authUser, final int failedLoginAttempts, final Timestamp lastLoginAttempt, final Timestamp lastSuccessfulLogin) {
        _authMethod = authMethod;
        _authMethodId = authMethodId;
        _authUser = authUser;
        _failedLoginAttempts = failedLoginAttempts;
        _lastLoginAttempt = lastLoginAttempt;
        _lastSuccessfulLogin = lastSuccessfulLogin;
    }

    public static final RowMapper<UserAuth> Mapper = (resultSet, index) -> {
        final Timestamp lastLoginAttempt    = resultSet.getTimestamp("last_login_attempt");
        final Timestamp lastSuccessfulLogin = resultSet.getTimestamp("last_successful_login");
        return new UserAuth(resultSet.getString("auth_method"),
                            resultSet.getString("auth_method_id"),
                            resultSet.getString("auth_user"),
                            resultSet.getInt("failed_login_attempts"),
                            lastLoginAttempt, lastSuccessfulLogin);
    };

    @Override
    public String toString() {
        return "class UserAuth {\n"
               + "  xdatUsername: " + getXdatUsername() + "\n"
               + "  authUser: " + getAuthUser() + "\n"
               + "  authMethod: " + getAuthMethod() + "\n"
               + "  authMethodId: " + getAuthMethodId() + "\n"
               + "  failedLoginAttempts: " + getFailedLoginAttempts() + "\n"
               + "  lastLoginAttempt: " + getLastLoginAttempt() + "\n"
               + "  lastSuccessfulLogin: " + getLastSuccessfulLogin() + "\n"
               + "  passwordUpdated: " + getPasswordUpdated() + "\n"
               + "  authorities: " + StringUtils.join(getAuthorities(), ", ") + "\n"
               + "}\n";
    }

    private String       _xdatUsername        = null;
    private String       _authUser            = null;
    private String       _authMethod          = null;
    private String       _authMethodId        = null;
    private Date         _lastLoginAttempt    = null;
    private Date         _lastSuccessfulLogin = null;
    private Integer      _failedLoginAttempts = null;
    private Date         _lockoutTime         = null;
    private Date         _passwordUpdated     = null;
    private List<String> _authorities         = new ArrayList<>();
}
