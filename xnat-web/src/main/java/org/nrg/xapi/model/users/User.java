package org.nrg.xapi.model.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
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
        _firstName = user.getFirstname();
        _lastName = user.getLastname();
        _email = user.getEmail();
        _isAdmin = (user instanceof XDATUser && ((XDATUser) user).isSiteAdmin());
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
    @JsonProperty("firstName")
    public String getFirstName() {
        return _firstName;
    }

    @SuppressWarnings("unused")
    public void setFirstName(String firstName) {
        _firstName = firstName;
    }

    /**
     * The user's last name.
     **/
    @ApiModelProperty(value = "The user's last name.")
    @JsonProperty("lastName")
    public String getLastName() {
        return _lastName;
    }

    @SuppressWarnings("unused")
    public void setLastName(String lastName) {
        _lastName = lastName;
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

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public void setAuthorization(UserAuth authorization) {
        _authorization = authorization;
    }

    @JsonIgnore
    public String getFullName() {
        return String.format("%s %s", getFirstName(), getLastName());
    }

    @JsonIgnore
    public XdatUser getXDATUser(final UserI requester) {
        final XdatUser user;
        if (StringUtils.isNotBlank(_username)) {
            user = XDATUser.getXdatUsersByLogin(_username, requester, true);
        } else if (_id != null) {
            user = AutoXdatUser.getXdatUsersByXdatUserId(_id, requester, true);
        } else {
            user = null;
        }
        if (user != null) {
            return user;
        }
        final XDATUser newUser = new XDATUser();
        newUser.setLogin(_username);
        newUser.setFirstname(_firstName);
        newUser.setLastname(_lastName);
        newUser.setEmail(_email);
        newUser.setPrimaryPassword(_password);
        newUser.setSalt(_salt);
        return newUser;
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

    private Integer _id        = null;
    private String  _username  = null;
    private String  _firstName = null;
    private String  _lastName  = null;
    private String  _email     = null;
    private boolean _isAdmin;
    private String _dbName = null;
    private String _password = null;
    private String _salt = null;
    private Date _lastModified = null;
    private UserAuth _authorization = null;
}
