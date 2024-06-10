/*
 * web: org.nrg.xapi.model.users.User
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.model.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.entities.UserAuthI;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xft.utils.DateUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Date;
import java.util.List;

/**
 * A transport container for user details. The {@link #isSecured() secured property} controls whether security-related
 * properties like password are available. When a new user object is created from an existing user record in XNAT, the
 * secure flag is set to true. This prevents serializing beans with existing user accounts to prevent exposing password
 * data. Newly created beans have secure set to false by default to allow for serializing the bean for REST calls with
 * all data intact.
 */
@ApiModel(description = "Contains the properties that define a user on the system.")
@Data
@Accessors(prefix = "_")
@AllArgsConstructor
@NoArgsConstructor
public class User {
    public static List<User> getAllUsers(final NamedParameterJdbcTemplate template) {
        return template.query(QUERY_ORDERED_PROFILES, USER_ROW_MAPPER);
    }

    public static List<User> getCurrentUsers(final NamedParameterJdbcTemplate template, final int maxLoginInterval, final int lastModifiedInterval) {
        return template.query(QUERY_CURRENT_USERS, new MapSqlParameterSource("maxLoginInterval", maxLoginInterval).addValue("lastModifiedInterval", lastModifiedInterval), USER_ROW_MAPPER);
    }

    public static User getUser(final NamedParameterJdbcTemplate template, final String username) throws NotFoundException {
        try {
            return template.queryForObject(QUERY_USER_PROFILE, new MapSqlParameterSource("username", username), USER_ROW_MAPPER);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(XdatUser.SCHEMA_ELEMENT_NAME, username);
        }
    }

    private static final RowMapper<User> USER_ROW_MAPPER        = (resultSet, index) -> new User(resultSet.getInt("id"),
                                                                                                 resultSet.getString("username"),
                                                                                                 resultSet.getString("firstName"),
                                                                                                 resultSet.getString("lastName"),
                                                                                                 resultSet.getString("email"),
                                                                                                 null,
                                                                                                 true,
                                                                                                 DateUtils.getDateForTimestamp(resultSet.getTimestamp("last_modified")),
                                                                                                 null,
                                                                                                 resultSet.getInt("enabled") == 1,
                                                                                                 resultSet.getInt("verified") == 1,
                                                                                                 DateUtils.getDateForTimestamp(resultSet.getTimestamp("last_successful_login")),
                                                                                                 resultSet.getString("pendingEmail"),
                                                                                                 null);
    private static final String          QUERY_USER_PROFILES    = "SELECT u.login AS username, u.xdat_user_id AS id, u.firstname, u.lastname, u.email, u.enabled, u.verified, m.last_modified, a.max_login AS last_successful_login, new_value as pendingEmail FROM xdat_user u JOIN xdat_user_meta_data m ON u.user_info = m.meta_data_id JOIN (SELECT xdat_username, max(last_successful_login) max_login FROM xhbm_xdat_user_auth GROUP BY xdat_username) a ON u.login = a.xdat_username LEFT JOIN xhbm_user_change_request ucr ON u.login = ucr.username AND ucr.field_to_change='email' AND ucr.enabled='t' ";
    private static final String          QUERY_ORDER_BY         = "ORDER BY u.xdat_user_id";
    private static final String          QUERY_LIMIT_TO_CURRENT = "WHERE u.enabled = 1 OR ((a.max_login > CURRENT_DATE - (INTERVAL '1 year' * :maxLoginInterval) OR (a.max_login IS NULL AND (m.last_modified > CURRENT_DATE - (INTERVAL '1 year' * :lastModifiedInterval)))))";
    private static final String          QUERY_LIMIT_TO_USER    = "WHERE u.login=:username";
    private static final String          QUERY_ORDERED_PROFILES = String.join(" ", QUERY_USER_PROFILES, QUERY_ORDER_BY);
    private static final String          QUERY_CURRENT_USERS    = String.join(" ", QUERY_USER_PROFILES, QUERY_LIMIT_TO_CURRENT, QUERY_ORDER_BY);
    private static final String          QUERY_USER_PROFILE     = String.join(" ", QUERY_USER_PROFILES, QUERY_LIMIT_TO_USER);

    /**
     * The user's encrypted password.
     **/
    @ApiModelProperty(value = "The user's encrypted password.")
    public String getPassword() {
        return getSecuredProperty(_password);
    }

    /**
     * The salt used to encrypt the user's _password.
     *
     * @return The user's salt
     *
     * @deprecated Passwords are automatically salted by the security framework.
     */
    @Deprecated
    @ApiModelProperty(value = "The salt used to encrypt the user's password.")
    public String getSalt() {
        return null;
    }

    /**
     * The user's authorization record used when logging in.
     **/
    @ApiModelProperty(value = "The user's authorization record used when logging in.")
    public UserAuthI getAuthorization() {
        return getSecuredProperty(_authorization);
    }

    @ApiModelProperty(value = "The user's full name.")
    @JsonIgnore
    public String getFullName() {
        return String.format("%s %s", getFirstName(), getLastName());
    }

    @Override
    public String toString() {
        return "class User {\n" +
               "  id: " + _id + "\n" +
               "  username: " + _username + "\n" +
               "  firstName: " + _firstName + "\n" +
               "  lastName: " + _lastName + "\n" +
               "  email: " + _email + "\n" +
               "  password: " + _password + "\n" +
               "  lastModified: " + _lastModified + "\n" +
               "  lastSuccessfulLogin: " + _lastSuccessfulLogin + "\n" +
               "  authorization: " + _authorization + "\n" +
               "  pendingEmail: " + _pendingEmail + "\n" +
                "}\n";
    }

    private <T> T getSecuredProperty(final T property) {
        return _secured ? null : property;
    }

    @ApiModelProperty(value = "The user's unique key.")
    private Integer   _id;
    @ApiModelProperty(value = "The user's login name.")
    private String    _username;
    @ApiModelProperty(value = "The user's first name.")
    private String    _firstName;
    @ApiModelProperty(value = "The user's last name.")
    private String    _lastName;
    @ApiModelProperty(value = "The user's email address.")
    private String    _email;
    @ApiModelProperty(value = "The user's encrypted password.")
    private String    _password;
    @ApiModelProperty(value = "Indicates whether the user object is secured, which causes secure fields like password to return null.")
    private boolean   _secured;
    @ApiModelProperty(value = "The date and time the user record was last modified.")
    private Date      _lastModified;
    @ApiModelProperty(value = "The user's authorization record used when logging in.")
    private UserAuthI _authorization;
    @ApiModelProperty(value = "Whether the user is enabled.")
    private Boolean   _enabled;
    @ApiModelProperty(value = "Whether the user is verified.")
    private Boolean   _verified;
    @ApiModelProperty("The date and time of the last successful login attempt for the most recently used authentication provider.")
    private Date      _lastSuccessfulLogin;
    @ApiModelProperty("New email address, pending verification")
    private String    _pendingEmail;
    @ApiModelProperty("If changing password, include current one")
    private String    _currentPassword;
}
