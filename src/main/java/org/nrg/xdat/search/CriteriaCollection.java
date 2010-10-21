//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 11, 2005
 *
 */
package org.nrg.xdat.search;

/**
 * @author Tim
 *
 * FindBugs says this class should be renamed to something other than CriteriaCollection.  I agree.  However,
 * this code is used in alot of places and is due for a big refactoring.  I vote for putting off this fix
 * until the search is refactored.  Unable to suppress warnings to support 1.5. 
 * 
 * Actually, Why not just delete this class.  What value is it adding?
 */
public class CriteriaCollection extends org.nrg.xft.search.CriteriaCollection{
	public CriteriaCollection(String join)
	{
		super(join);
	}
	
	
}

