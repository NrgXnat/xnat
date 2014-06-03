/*
 * org.nrg.xdat.om.XdatSecurityI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.om;
import java.util.ArrayList;

import org.nrg.xft.ItemI;

/**
 * @author XDAT
 *
 */
public interface XdatSecurityI {

	public String getSchemaElementName();

	/**
	 * users/user
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatUserI
	 */
	public ArrayList getUsers_user();

	/**
	 * Sets the value for users/user.
	 * @param v Value to Set.
	 */
	public void setUsers_user(ItemI v) throws Exception;

	/**
	 * roles/role
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatRoleTypeI
	 */
	public ArrayList getRoles_role();

	/**
	 * Sets the value for roles/role.
	 * @param v Value to Set.
	 */
	public void setRoles_role(ItemI v) throws Exception;

	/**
	 * actions/action
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatActionTypeI
	 */
	public ArrayList getActions_action();

	/**
	 * Sets the value for actions/action.
	 * @param v Value to Set.
	 */
	public void setActions_action(ItemI v) throws Exception;

	/**
	 * element_security_set/element_security
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatElementSecurityI
	 */
	public ArrayList getElementSecuritySet_elementSecurity();

	/**
	 * Sets the value for element_security_set/element_security.
	 * @param v Value to Set.
	 */
	public void setElementSecuritySet_elementSecurity(ItemI v) throws Exception;

	/**
	 * @return Returns the system.
	 */
	public String getSystem();

	/**
	 * Sets the value for system.
	 * @param v Value to Set.
	 */
	public void setSystem(String v);

	/**
	 * @return Returns the require_login.
	 */
	public Boolean getRequireLogin();

	/**
	 * Sets the value for require_login.
	 * @param v Value to Set.
	 */
	public void setRequireLogin(Object v);

	/**
	 * @return Returns the xdat_security_id.
	 */
	public Integer getXdatSecurityId();

	/**
	 * Sets the value for xdat_security_id.
	 * @param v Value to Set.
	 */
	public void setXdatSecurityId(Integer v);
}
