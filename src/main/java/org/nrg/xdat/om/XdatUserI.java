// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:52 CST 2007
 *
 */
package org.nrg.xdat.om;
import java.util.ArrayList;

import org.nrg.xft.ItemI;

/**
 * @author XDAT
 *
 */
public interface XdatUserI {

	public String getSchemaElementName();

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
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatElementAccessI
	 */
	public ArrayList getElementAccess();

	/**
	 * Sets the value for element_access.
	 * @param v Value to Set.
	 */
	public void setElementAccess(ItemI v) throws Exception;

	/**
	 * assigned_roles/assigned_role
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatRoleTypeI
	 */
	public ArrayList getAssignedRoles_assignedRole();

	/**
	 * Sets the value for assigned_roles/assigned_role.
	 * @param v Value to Set.
	 */
	public void setAssignedRoles_assignedRole(ItemI v) throws Exception;

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
	 * @return Returns the xdat_user_id.
	 */
	public Integer getXdatUserId();

	/**
	 * Sets the value for xdat_user_id.
	 * @param v Value to Set.
	 */
	public void setXdatUserId(Integer v);
}
