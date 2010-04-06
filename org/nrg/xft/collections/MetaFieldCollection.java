//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 4, 2005
 *
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

