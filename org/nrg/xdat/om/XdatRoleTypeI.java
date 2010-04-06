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
public interface XdatRoleTypeI {

	public String getSchemaElementName();

	/**
	 * allowed_actions/allowed_action
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatActionTypeI
	 */
	public ArrayList getAllowedActions_allowedAction();

	/**
	 * Sets the value for allowed_actions/allowed_action.
	 * @param v Value to Set.
	 */
	public void setAllowedActions_allowedAction(ItemI v) throws Exception;

	/**
	 * @return Returns the role_name.
	 */
	public String getRoleName();

	/**
	 * Sets the value for role_name.
	 * @param v Value to Set.
	 */
	public void setRoleName(String v);

	/**
	 * @return Returns the description.
	 */
	public String getDescription();

	/**
	 * Sets the value for description.
	 * @param v Value to Set.
	 */
	public void setDescription(String v);

	/**
	 * @return Returns the sequence.
	 */
	public Integer getSequence();

	/**
	 * Sets the value for sequence.
	 * @param v Value to Set.
	 */
	public void setSequence(Integer v);
}
