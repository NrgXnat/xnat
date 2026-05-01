/*
 * core: org.nrg.xft.collections.MetaFieldCollection
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.collections;

import java.util.Iterator;

import org.nrg.xft.schema.Wrappers.GenericWrapper.MetaField;

/**
 * @author Tim
 *
 */
public class MetaFieldCollection extends XFTCollection {
	public void addField(MetaField mf)
	{
		this.addStoredItem(mf);
	}
	
	public MetaField getField(String id)
	{
		return (MetaField)this.getStoredItem(id);
	}
	
	public java.util.Collection getCollection()
	{
		return this.getItemHash().values();
	}
	
	public Iterator getIterator()
	{
		return this.getItemIterator();
	}
}

