/*
 * core: org.nrg.xdat.collections.DisplayFieldCollection
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.nrg.xdat.display.DisplayField;
import org.nrg.xft.collections.XFTCollection;
import org.nrg.xft.exception.DuplicateKeyException;
import org.nrg.xft.exception.ItemNotFoundException;
import org.nrg.xft.sequence.SequenceComparator;

/**
 * @author Tim
 *
 */
public class DisplayFieldCollection extends XFTCollection {

	public DisplayFieldCollection()
	{
		super();
		setAllowReplacement(false);
	}

	public DisplayField getDisplayField(String id)
	{
			return (DisplayField)this.getStoredItem(id);
	}

	public void addDisplayField(DisplayField df)
	{
	    	df.setSortIndex(this.getItemHash().size());
			this.addStoredItem(df);
	}

	public DisplayField getDisplayFieldWException(String id) throws DisplayFieldNotFoundException
	{
		try {
			return (DisplayField)this.getStoredItemWException(id);
		} catch (ItemNotFoundException e) {
			throw new DisplayFieldNotFoundException(e);
		}
	}

	public void addDisplayFieldWException(DisplayField df) throws DuplicateDisplayFieldException
	{
		try {
		    df.setSortIndex(this.getItemHash().size());
			this.addStoredItemWException(df);
		} catch (DuplicateKeyException e) {
			throw new DuplicateDisplayFieldException(df.getParentDisplay().getElementName(),df.getId());
		}
	}

	public ArrayList<DisplayField> getSortedFields()
	{
	    ArrayList<DisplayField> al = new ArrayList<DisplayField>();
	    al.addAll(this.getItemHash().values());
	    Collections.sort(al,SequenceComparator.SequenceComparator);
	    return al;
	}

	public Iterator getDisplayFieldIterator()
	{
		return this.getItemIterator();
	}

	public java.util.Hashtable getDisplayFieldHash()
	{
		return this.getItemHash();
	}

    @SuppressWarnings("serial")
	public class DisplayFieldNotFoundException extends Exception{
		public DisplayFieldNotFoundException(ItemNotFoundException e)
		{
			super("Display Field not found: '" + e.ELEMENT + "'");
		}
	}

    @SuppressWarnings("serial")
	public class DuplicateDisplayFieldException extends Exception{
		public DuplicateDisplayFieldException(org.nrg.xft.exception.DuplicateKeyException e)
		{
			super("Duplicate Display Field: '" + e.ELEMENT + "'.'" + e.FIELD + "'");
		}
		public DuplicateDisplayFieldException(String element, String field)
		{
			super("Duplicate Display Field: '" + element + "'.'" + field + "'");
		}
		public DuplicateDisplayFieldException(String field)
		{
			super("Duplicate Display Field: '" + field + "'");
		}
	}
}

