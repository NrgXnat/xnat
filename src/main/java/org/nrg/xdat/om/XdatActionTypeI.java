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
public interface XdatActionTypeI {

	public String getSchemaElementName();

	/**
	 * @return Returns the action_name.
	 */
	public String getActionName();

	/**
	 * Sets the value for action_name.
	 * @param v Value to Set.
	 */
	public void setActionName(String v);

	/**
	 * @return Returns the display_name.
	 */
	public String getDisplayName();

	/**
	 * Sets the value for display_name.
	 * @param v Value to Set.
	 */
	public void setDisplayName(String v);

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
