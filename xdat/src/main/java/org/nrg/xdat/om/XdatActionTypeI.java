/*
 * core: org.nrg.xdat.om.XdatActionTypeI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om;

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
