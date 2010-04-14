// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Tue Oct 23 10:47:21 CDT 2007
 *
 */
package org.nrg.xdat.om;

/**
 * @author XDAT
 *
 */
public interface XdatNewsentryI {

	public String getSchemaElementName();

	/**
	 * @return Returns the date.
	 */
	public Object getDate();

	/**
	 * Sets the value for date.
	 * @param v Value to Set.
	 */
	public void setDate(Object v);

	/**
	 * @return Returns the title.
	 */
	public String getTitle();

	/**
	 * Sets the value for title.
	 * @param v Value to Set.
	 */
	public void setTitle(String v);

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
	 * @return Returns the link.
	 */
	public String getLink();

	/**
	 * Sets the value for link.
	 * @param v Value to Set.
	 */
	public void setLink(String v);

	/**
	 * @return Returns the xdat_newsEntry_id.
	 */
	public Integer getXdatNewsentryId();

	/**
	 * Sets the value for xdat_newsEntry_id.
	 * @param v Value to Set.
	 */
	public void setXdatNewsentryId(Integer v);
}
