// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:52 CST 2007
 *
 */
package org.nrg.xdat.om;
import org.nrg.xft.*;
import org.nrg.xft.security.UserI;
import org.nrg.xdat.om.*;

import java.util.*;

/**
 * @author XDAT
 *
 */
public interface XdatElementAccessI {

	public String getSchemaElementName();

	/**
	 * @return Returns the secondary_password.
	 */
	public String getSecondaryPassword();

	/**
	 * Sets the value for secondary_password.
	 * @param v Value to Set.
	 */
	public void setSecondaryPassword(String v);

	/**
	 * @return Returns the secondary_password/encrypt.
	 */
	public Boolean getSecondaryPassword_encrypt();

	/**
	 * Sets the value for secondary_password/encrypt.
	 * @param v Value to Set.
	 */
	public void setSecondaryPassword_encrypt(Object v);

	/**
	 * secure_ip
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatElementAccessSecureIpI
	 */
	public ArrayList getSecureIp();

	/**
	 * Sets the value for secure_ip.
	 * @param v Value to Set.
	 */
	public void setSecureIp(ItemI v) throws Exception;

	/**
	 * permissions/allow_set
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatFieldMappingSetI
	 */
	public ArrayList getPermissions_allowSet();

	/**
	 * Sets the value for permissions/allow_set.
	 * @param v Value to Set.
	 */
	public void setPermissions_allowSet(ItemI v) throws Exception;

	/**
	 * @return Returns the element_name.
	 */
	public String getElementName();

	/**
	 * Sets the value for element_name.
	 * @param v Value to Set.
	 */
	public void setElementName(String v);

	/**
	 * @return Returns the xdat_element_access_id.
	 */
	public Integer getXdatElementAccessId();

	/**
	 * Sets the value for xdat_element_access_id.
	 * @param v Value to Set.
	 */
	public void setXdatElementAccessId(Integer v);
}
