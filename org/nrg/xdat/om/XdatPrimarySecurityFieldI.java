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
public interface XdatPrimarySecurityFieldI {

	public String getSchemaElementName();

	/**
	 * @return Returns the primary_security_field.
	 */
	public String getPrimarySecurityField();

	/**
	 * Sets the value for primary_security_field.
	 * @param v Value to Set.
	 */
	public void setPrimarySecurityField(String v);

	/**
	 * @return Returns the xdat_primary_security_field_id.
	 */
	public Integer getXdatPrimarySecurityFieldId();

	/**
	 * Sets the value for xdat_primary_security_field_id.
	 * @param v Value to Set.
	 */
	public void setXdatPrimarySecurityFieldId(Integer v);
}
