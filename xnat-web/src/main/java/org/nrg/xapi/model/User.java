package org.nrg.xapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang.StringUtils;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.om.base.auto.AutoXdatUser;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xft.security.UserI;

import java.util.Date;

@ApiModel(description = "Contains the properties that define a user on the system.")
public class User {
    public User() {
    }

    public User(final String username) {
        this(XDATUser.getXdatUsersByLogin(username, null, false));
    }

    public User(final UserI user) {
        _id = user.getID();
        _username = user.getUsername();
        _firstname = user.getFirstname();
        _lastname = user.getLastname();
        _email = user.getEmail();
        _isAdmin = (user instanceof XDATUser ? ((XDATUser) user).isSiteAdmin() : false);
        _dbName = "";
        _password = "";
        _salt = "";
        _lastModified = null;
        _authorization = null;
    }

    public User(final XdatUser user) {
        this((UserI) user);
    }

    /**
     * The user's unique key.
     **/
    @ApiModelProperty(value = "The user's unique key.")
    @JsonProperty("id")
    public Integer getId() {
        return _id;
    }

    public void setId(Integer id) {
        _id = id;
    }

    /**
     * The user's login name.
     **/
    @ApiModelProperty(value = "The user's login name.")
    @JsonProperty("username")
    public String getUsername() {
        return _username;
    }

    public void setUsername(String username) {
        _username = username;
    }

    /**
     * The user's first name.
     **/
    @ApiModelProperty(value = "The user's first name.")
    @JsonProperty("firstname")
    public String getFirstname() {
        return _firstname;
    }

    public void setFirstname(String firstname) {
        _firstname = firstname;
    }

    /**
     * The user's last name.
     **/
    @ApiModelProperty(value = "The user's last name.")
    @JsonProperty("lastname")
    public String getLastname() {
        return _lastname;
    }

    public void setLastname(String lastname) {
        _lastname = lastname;
    }

    /**
     * The user's _email address.
     **/
    @ApiModelProperty(value = "The user's email address.")
    @JsonProperty("email")
    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }

    /**
     * Whether the user is a site administrator.
     **/
    @ApiModelProperty(value = "Whether the user is a site administrator.")
    @JsonProperty("admin")
    public boolean isAdmin() {
        return _isAdmin;
    }

    public void setAdmin(final boolean isAdmin) {
        _isAdmin = isAdmin;
    }

    /**
     * The user's primary database (deprecated).
     **/
    @ApiModelProperty(value = "The user's primary database (deprecated).")
    @JsonProperty("dbName")
    public String getDbName() {
        return _dbName;
    }

    public void setDbName(String dbName) {
        _dbName = dbName;
    }

    /**
     * The user's encrypted _password.
     **/
    @ApiModelProperty(value = "The user's encrypted password.")
    @JsonProperty("password")
    public String getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        _password = password;
    }

    /**
     * The _salt used to encrypt the user's _password.
     **/
    @ApiModelProperty(value = "The salt used to encrypt the user's password.")
    @JsonProperty("salt")
    public String getSalt() {
        return _salt;
    }

    public void setSalt(String salt) {
        _salt = salt;
    }

    /**
     * The date and time the user record was last modified.
     **/
    @ApiModelProperty(value = "The date and time the user record was last modified.")
    @JsonProperty("lastModified")
    public Date getLastModified() {
        return _lastModified;
    }

    public void setLastModified(Date lastModified) {
        _lastModified = lastModified;
    }

    /**
     * The user's authorization record used when logging in.
     **/
    @ApiModelProperty(value = "The user's authorization record used when logging in.")
    @JsonProperty("authorization")
    public UserAuth getAuthorization() {
        return _authorization;
    }

    public void setAuthorization(UserAuth authorization) {
        _authorization = authorization;
    }

    @JsonIgnore
    public String getFullname() {
        return String.format("%s %s", getFirstname(), getLastname());
    }

    @JsonIgnore
    public XdatUser getXDATUser(final UserI requestor) {
        final XdatUser user;
        if (StringUtils.isNotBlank(_username)) {
            user = XDATUser.getXdatUsersByLogin(_username, requestor, true);
        } else if (_id != null) {
            user = AutoXdatUser.getXdatUsersByXdatUserId(_id, requestor, true);
        } else {
            user = null;
        }
        if (user != null) {
            return user;
        }
        final XDATUser newUser = new XDATUser();
        newUser.setLogin(_username);
        newUser.setFirstname(_firstname);
        newUser.setLastname(_lastname);
        newUser.setEmail(_email);
        newUser.setPrimaryPassword(_password);
        newUser.setSalt(_salt);
        return newUser;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class User {\n");

        sb.append("  id: ").append(_id).append("\n");
        sb.append("  username: ").append(_username).append("\n");
        sb.append("  firstname: ").append(_firstname).append("\n");
        sb.append("  lastname: ").append(_lastname).append("\n");
        sb.append("  email: ").append(_email).append("\n");
        sb.append("  dbName: ").append(_dbName).append("\n");
        sb.append("  password: ").append(_password).append("\n");
        sb.append("  salt: ").append(_salt).append("\n");
        sb.append("  lastModified: ").append(_lastModified).append("\n");
        sb.append("  authorization: ").append(_authorization).append("\n");
        sb.append("}\n");
        return sb.toString();
    }

    private Integer _id = null;
    private String _username = null;
    private String _firstname = null;
    private String _lastname = null;
    private String _email = null;
    private boolean _isAdmin;
    private String _dbName = null;
    private String _password = null;
    private String _salt = null;
    private Date _lastModified = null;
    private UserAuth _authorization = null;
}
