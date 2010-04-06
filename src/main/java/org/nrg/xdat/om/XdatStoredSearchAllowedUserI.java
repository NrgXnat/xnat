// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:53 CST 2007
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
public interface XdatStoredSearchAllowedUserI {

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
	 * @return Returns the xdat_stored_search_allowed_user_id.
	 */
	public Integer getXdatStoredSearchAllowedUserId();

	/**
	 * Sets the value for xdat_stored_search_allowed_user_id.
	 * @param v Value to Set.
	 */
	public void setXdatStoredSearchAllowedUserId(Integer v);
}
