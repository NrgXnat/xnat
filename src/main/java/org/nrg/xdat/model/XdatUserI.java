/*
 * core: org.nrg.xdat.model.XdatUserI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*
 * GENERATED FILE
 * Created on Thu Mar 31 18:38:30 CDT 2016
 *
 */
package org.nrg.xdat.model;

import java.util.List;

/**
 * @author XDAT
 *
 */
public interface XdatUserI {

	public String getXSIType();

	public void toXML(java.io.Writer writer) throws java.lang.Exception;

	/**
	 * @return Returns the login.
	 */
	public String getLogin();

	/**
	 * Sets the value for login.
	 * @param v Value to Set.
	 */
	public void setLogin(String v);

	/**
	 * @return Returns the firstname.
	 */
	public String getFirstname();

	/**
	 * Sets the value for firstname.
	 * @param v Value to Set.
	 */
	public void setFirstname(String v);

	/**
	 * @return Returns the lastname.
	 */
	public String getLastname();

	/**
	 * Sets the value for lastname.
	 * @param v Value to Set.
	 */
	public void setLastname(String v);

	/**
	 * @return Returns the email.
	 */
	public String getEmail();

	/**
	 * Sets the value for email.
	 * @param v Value to Set.
	 */
	public void setEmail(String v);

	/**
	 * @return Returns the primary_password.
	 */
	public String getPrimaryPassword();

	/**
	 * Sets the value for primary_password.
	 * @param v Value to Set.
	 */
	public void setPrimaryPassword(String v);

	/**
	 * @return Returns the primary_password/encrypt.
	 */
	public Boolean getPrimaryPassword_encrypt();

	/**
	 * Sets the value for primary_password/encrypt.
	 * @param v Value to Set.
	 */
	public void setPrimaryPassword_encrypt(Object v);

	/**
	 * element_access
	 * @return Returns an List of org.nrg.xdat.model.XdatElementAccessI
	 */
	public <A extends org.nrg.xdat.model.XdatElementAccessI> List<A> getElementAccess();

	/**
	 * element_access
	 * @return Returns an List of org.nrg.xdat.model.XdatElementAccessI
	 */
	public <A extends org.nrg.xdat.model.XdatElementAccessI> void addElementAccess(A item) throws Exception;

	/**
	 * assigned_roles/assigned_role
	 * @return Returns an List of org.nrg.xdat.model.XdatRoleTypeI
	 */
	public <A extends org.nrg.xdat.model.XdatRoleTypeI> List<A> getAssignedRoles_assignedRole();

	/**
	 * assigned_roles/assigned_role
	 * @return Returns an List of org.nrg.xdat.model.XdatRoleTypeI
	 */
	public <A extends org.nrg.xdat.model.XdatRoleTypeI> void addAssignedRoles_assignedRole(A item) throws Exception;

	/**
	 * @return Returns the quarantine_path.
	 */
	public String getQuarantinePath();

	/**
	 * Sets the value for quarantine_path.
	 * @param v Value to Set.
	 */
	public void setQuarantinePath(String v);

	/**
	 * groups/groupID
	 * @return Returns an List of org.nrg.xdat.model.XdatUserGroupidI
	 */
	public <A extends org.nrg.xdat.model.XdatUserGroupidI> List<A> getGroups_groupid();

	/**
	 * groups/groupID
	 * @return Returns an List of org.nrg.xdat.model.XdatUserGroupidI
	 */
	public <A extends org.nrg.xdat.model.XdatUserGroupidI> void addGroups_groupid(A item) throws Exception;

	/**
	 * @return Returns the enabled.
	 */
	public Boolean getEnabled();

	/**
	 * Sets the value for enabled.
	 * @param v Value to Set.
	 */
	public void setEnabled(Object v);

	/**
	 * @return Returns the verified.
	 */
	public Boolean getVerified();

	/**
	 * Sets the value for verified.
	 * @param v Value to Set.
	 */
	public void setVerified(Object v);

	/**
	 * @return Returns the salt.
	 */
	public String getSalt();

	/**
	 * Sets the value for salt.
	 * @param v Value to Set.
	 */
	public void setSalt(String v);

	/**
	 * @return Returns the xdat_user_id.
	 */
	public Integer getXdatUserId();
}
