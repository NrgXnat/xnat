/*
 * org.nrg.xft.search.SQLClause
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.search;

import java.util.ArrayList;

import org.nrg.xdat.search.DisplayCriteria;


/**
 * @author Tim
 *
 */
public interface SQLClause {
    public String getElementName();
	//public String getSQLClause() throws Exception;
	public String getSQLClause(QueryOrganizerI qo) throws Exception;
	public ArrayList getSchemaFields() throws Exception;
    public ArrayList<DisplayCriteria> getSubQueries() throws Exception;
	public int numClauses();
}

