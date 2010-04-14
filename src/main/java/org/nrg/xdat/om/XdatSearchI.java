// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Thu Sep 06 11:16:12 CDT 2007
 *
 */
package org.nrg.xdat.om;
import org.nrg.xft.ItemI;

/**
 * @author XDAT
 *
 */
public interface XdatSearchI extends XdatStoredSearchI {

	public String getSchemaElementName();

	/**
	 * stored_search
	 * @return org.nrg.xdat.om.XdatStoredSearchI
	 */
	public org.nrg.xdat.om.XdatStoredSearchI getStoredSearch();

	/**
	 * Sets the value for stored_search.
	 * @param v Value to Set.
	 */
	public void setStoredSearch(ItemI v) throws Exception;

	/**
	 * @return Returns the page.
	 */
	public Integer getPage();

	/**
	 * Sets the value for page.
	 * @param v Value to Set.
	 */
	public void setPage(Integer v);
}
