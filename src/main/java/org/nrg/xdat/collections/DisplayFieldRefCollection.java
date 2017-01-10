/*
 * core: org.nrg.xdat.collections.DisplayFieldRefCollection
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.collections;

import java.util.ArrayList;
import java.util.Collections;

import org.nrg.xdat.display.DisplayFieldRef;
import org.nrg.xdat.sortable.Sortable;
import org.nrg.xft.collections.XFTCollection;
/**
 * @author Tim
 *
 */
public class DisplayFieldRefCollection extends XFTCollection {
	private static int fieldRefCounter = 0;
	private ArrayList coll = null;
	public DisplayFieldRefCollection()
	{
		super();
		setAllowReplacement(false);
	}
	
	public DisplayFieldRef getDisplayField(String id) throws DisplayFieldRefNotFoundException
	{
			return (DisplayFieldRef)this.getStoredItem(id);
	}
	
	public void addDisplayField(DisplayFieldRef df) throws DuplicateDisplayFieldRefException
	{
			df.setSortOrder(fieldRefCounter++);
			this.addStoredItem(df);
	}
	
	public DisplayFieldRef getDisplayFieldWException(String id) throws DisplayFieldRefNotFoundException
	{
		try {
			return (DisplayFieldRef)this.getStoredItemWException(id);
		} catch (org.nrg.xft.exception.ItemNotFoundException e) {
			throw new DisplayFieldRefNotFoundException(e);
		}
	}
	
	public void addDisplayFieldWException(DisplayFieldRef df) throws DuplicateDisplayFieldRefException
	{
		try {
			this.addStoredItemWException(df);
		} catch (org.nrg.xft.exception.DuplicateKeyException e) {
			throw new DuplicateDisplayFieldRefException(e);
		}
	}
	
	public ArrayList getSortedDisplayFieldRefs()
	{
	    if (coll == null)
		{
			coll = new java.util.ArrayList();
			if (getItemHash() != null){
				coll.addAll(getItemHash().values());
				Collections.sort(coll,Sortable.SequenceComparator);
			}else{
				return new ArrayList();
			}
		}
	    return coll;
	}
	
	public java.util.Iterator getDisplayFieldRefIterator()
	{
		return getSortedDisplayFieldRefs().iterator();
	}
	
	public java.util.Hashtable getDisplayFieldRefHash()
	{
		return this.getItemHash();
	}

    @SuppressWarnings("serial")
	public class DisplayFieldRefNotFoundException extends Exception{
		public DisplayFieldRefNotFoundException(org.nrg.xft.exception.ItemNotFoundException e)
		{
			super("Display Field Reference not found: '" + e.ELEMENT + "'");
		}
	}

    @SuppressWarnings("serial")
	public class DuplicateDisplayFieldRefException extends Exception{
		public DuplicateDisplayFieldRefException(org.nrg.xft.exception.DuplicateKeyException e)
		{
			super("Duplicate Display Field Reference: '" + e.FIELD + "'");
		}
	}
}

