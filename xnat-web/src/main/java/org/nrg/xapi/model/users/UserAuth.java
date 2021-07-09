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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.utils.DateUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Date;
import java.util.List;

@ApiModel(description = "Contains the properties that define a user's authentication provider mapping entry on the system.")
@Data
@Accessors(prefix = "_")
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@JsonPropertyOrder({"xdatUsername", "authMethod", "authMethodId", "authUser", "lastLoginAttempt", "lastSuccessfulLogin", "failedLoginAttempts"})
public class UserAuth {
    public static List<UserAuth> getUserAuths(final NamedParameterJdbcTemplate template, final String username) {
        return template.query(QUERY_USER_AUTHS, new MapSqlParameterSource("username", username), USER_AUTH_ROW_MAPPER);
    }

    private static final String              QUERY_USER_AUTHS     = "SELECT a.xdat_username, a.auth_method, a.auth_method_id, a.auth_user, a.last_login_attempt, a.last_successful_login, a.failed_login_attempts FROM xdat_user u JOIN xdat_user_meta_data m ON u.user_info = m.meta_data_id JOIN xhbm_xdat_user_auth a ON u.login = a.xdat_username WHERE u.login = :username";
    private static final RowMapper<UserAuth> USER_AUTH_ROW_MAPPER = (resultSet, index) -> new UserAuth(resultSet.getString("xdat_username"),
                                                                                                       resultSet.getString("auth_method"),
                                                                                                       resultSet.getString("auth_method_id"),
                                                                                                       resultSet.getString("auth_user"),
                                                                                                       DateUtils.getDateForTimestamp(resultSet.getTimestamp("last_login_attempt")),
                                                                                                       DateUtils.getDateForTimestamp(resultSet.getTimestamp("last_successful_login")),
                                                                                                       resultSet.getInt("failed_login_attempts"));

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
               + "}\n";
    }

    private String  _xdatUsername        = null;
    private String  _authMethod          = null;
    private String  _authMethodId        = null;
    private String  _authUser            = null;
    private Date    _lastLoginAttempt    = null;
    private Date    _lastSuccessfulLogin = null;
    private Integer _failedLoginAttempts = null;
}
