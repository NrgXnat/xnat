/*
 * core: org.nrg.xdat.collections.DisplayFieldCollection
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xft.collections.XFTCollection;
import org.nrg.xft.exception.DuplicateKeyException;
import org.nrg.xft.exception.ItemNotFoundException;
import org.nrg.xft.identifier.Identifier;
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

	@JsonIgnore
	public DisplayField getDisplayField(String id)
	{
			return (DisplayField)this.getStoredItem(id);
	}

	public void addDisplayField(DisplayField df)
	{
	    	df.setSortIndex(this.getItemHash().size());
			this.addStoredItem(df);
	}

	public void removeDisplayField(String displayFieldId){
		this.removeItemById(displayFieldId);
	}

	@JsonIgnore
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
			throw new DuplicateDisplayFieldException(df.getParentDisplay().getElementName(), df.getId());
		}
	}

	@JsonIgnore
	public List<DisplayField> getSortedFields()
	{
		List<DisplayField> al = getItemHash().values().stream().filter(identifier -> identifier instanceof DisplayField).map(DisplayField.class::cast).collect(Collectors.toList());
		Collections.sort(al, SequenceComparator.SequenceComparator);
		return al;
	}

	@JsonIgnore
	public Iterator<Identifier> getDisplayFieldIterator()
	{
		return this.getItemIterator();
	}

	public Map<String, Identifier> getDisplayFieldHash()
	{
		return this.getItemHash();
	}

    @SuppressWarnings("serial")
	public static class DisplayFieldNotFoundException extends Exception{
		public DisplayFieldNotFoundException(ItemNotFoundException e)
		{
			super("Display Field not found: '" + e.ELEMENT + "'", e);
		}
	}

    @SuppressWarnings("serial")
	public static class DuplicateDisplayFieldException extends Exception{
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

