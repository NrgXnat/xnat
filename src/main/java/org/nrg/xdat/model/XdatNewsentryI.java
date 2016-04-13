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
public interface XdatNewsentryI {

	public String getXSIType();

	public void toXML(java.io.Writer writer) throws java.lang.Exception;

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
}
