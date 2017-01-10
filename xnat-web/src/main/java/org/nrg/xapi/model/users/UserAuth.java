/*
 * web: org.nrg.xapi.model.users.UserAuth
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.model.users;

import java.util.Date;
import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@ApiModel(description = "")
public class UserAuth  {
  
  private String xdatUsername = null;
  private User authUser = null;
  private String authMethod = null;
  private String authMethodId = null;
  private Integer failedLoginAttempts = null;
  private Date lastSuccessfulLogin = null;
  private Date passwordUpdated = null;
  private List<String> authorities = new ArrayList<String>();

  
  /**
   * The login name of the user account with which this authentication method is associated.
   **/
  @ApiModelProperty(value = "The login name of the user account with which this authentication method is associated.")
  @JsonProperty("xdatUsername")
  public String getXdatUsername() {
    return xdatUsername;
  }
  public void setXdatUsername(String xdatUsername) {
    this.xdatUsername = xdatUsername;
  }

  
  /**
   * The user name for the authentication method.
   **/
  @ApiModelProperty(value = "The user name for the authentication method.")
  @JsonProperty("authUser")
  public User getAuthUser() {
    return authUser;
  }
  public void setAuthUser(User authUser) {
    this.authUser = authUser;
  }

  
  /**
   * How this method actually authenticates the user.
   **/
  @ApiModelProperty(value = "How this method actually authenticates the user.")
  @JsonProperty("authMethod")
  public String getAuthMethod() {
    return authMethod;
  }
  public void setAuthMethod(String authMethod) {
    this.authMethod = authMethod;
  }

  
  /**
   * The ID used to map properties for the authentication method.
   **/
  @ApiModelProperty(value = "The ID used to map properties for the authentication method.")
  @JsonProperty("authMethodId")
  public String getAuthMethodId() {
    return authMethodId;
  }
  public void setAuthMethodId(String authMethodId) {
    this.authMethodId = authMethodId;
  }

  
  /**
   * The number of failed login attempts for this user without a successful login.
   **/
  @ApiModelProperty(value = "The number of failed login attempts for this user without a successful login.")
  @JsonProperty("failedLoginAttempts")
  public Integer getFailedLoginAttempts() {
    return failedLoginAttempts;
  }
  public void setFailedLoginAttempts(Integer failedLoginAttempts) {
    this.failedLoginAttempts = failedLoginAttempts;
  }

  
  /**
   * The date and time the user last logged in with this authentication method.
   **/
  @ApiModelProperty(value = "The date and time the user last logged in with this authentication method.")
  @JsonProperty("lastSuccessfulLogin")
  public Date getLastSuccessfulLogin() {
    return lastSuccessfulLogin;
  }
  public void setLastSuccessfulLogin(Date lastSuccessfulLogin) {
    this.lastSuccessfulLogin = lastSuccessfulLogin;
  }

  
  /**
   * The date and time the user last updated their password.
   **/
  @ApiModelProperty(value = "The date and time the user last updated their password.")
  @JsonProperty("passwordUpdated")
  public Date getPasswordUpdated() {
    return passwordUpdated;
  }
  public void setPasswordUpdated(Date passwordUpdated) {
    this.passwordUpdated = passwordUpdated;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("authorities")
  public List<String> getAuthorities() {
    return authorities;
  }
  public void setAuthorities(List<String> authorities) {
    this.authorities = authorities;
  }

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class UserAuth {\n");
    
    sb.append("  xdatUsername: ").append(xdatUsername).append("\n");
    sb.append("  authUser: ").append(authUser).append("\n");
    sb.append("  authMethod: ").append(authMethod).append("\n");
    sb.append("  authMethodId: ").append(authMethodId).append("\n");
    sb.append("  failedLoginAttempts: ").append(failedLoginAttempts).append("\n");
    sb.append("  lastSuccessfulLogin: ").append(lastSuccessfulLogin).append("\n");
    sb.append("  passwordUpdated: ").append(passwordUpdated).append("\n");
    sb.append("  authorities: ").append(authorities).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
