//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 11, 2005
 *
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

