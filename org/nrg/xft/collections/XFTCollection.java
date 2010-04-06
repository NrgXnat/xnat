//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 10, 2005
 *
 */
package org.nrg.xft.collections;

import java.util.Hashtable;
import java.util.Iterator;

import org.nrg.xft.exception.DuplicateKeyException;
import org.nrg.xft.exception.ItemNotFoundException;
import org.nrg.xft.identifier.Identifier;

/**
 * @author Tim
 *
 */
public abstract class XFTCollection {
	private Hashtable coll = new Hashtable();
	private boolean allowReplacement = false;
	private boolean allowItemNotFoundError = true;

	protected Object getStoredItem(String id)
	{
		Object o = coll.get(id);
		if (o==null && allowItemNotFoundError)
		{
			return null;
		}else
			return coll.get(id);
	}

	protected void addStoredItem(Identifier o)
	{
		if (! allowReplacement)
		{
			if (coll.containsKey(o.getId()))
			{
			}else{
				coll.put(o.getId(),o);
			}
		}else{
			coll.put(o.getId(),o);
		}
	}

	protected Object getStoredItemWException(String id) throws ItemNotFoundException
	{
		Object o = coll.get(id);
		if (o==null && allowItemNotFoundError)
		{
			throw new ItemNotFoundException(id);
		}else
			return coll.get(id);
	}

	protected void addStoredItemWException(Identifier o) throws DuplicateKeyException
	{
		if (! allowReplacement)
		{
			if (coll.containsKey(o.getId()))
			{
				throw new DuplicateKeyException(o.getId());
			}else{
				coll.put(o.getId(),o);
			}
		}else{
			coll.put(o.getId(),o);
		}
	}

	protected java.util.Iterator getItemIterator()
	{
		return coll.values().iterator();
	}

	protected java.util.Hashtable getItemHash()
	{
		return coll;
	}

	/**
	 * @return
	 */
	public boolean allowReplacement() {
		return allowReplacement;
	}

	/**
	 * @param b
	 */
	public void setAllowReplacement(boolean b) {
		allowReplacement = b;
	}

	/**
	 * @return
	 */
	public boolean allowItemNotFoundError() {
		return allowItemNotFoundError;
	}

	/**
	 * @param b
	 */
	public void setAllowItemNotFoundError(boolean b) {
		allowItemNotFoundError = b;
	}

	public int size()
	{
	    return coll.size();
	}

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(coll.size() + " Items");
        Iterator iter = this.getItemIterator();
        while(iter.hasNext())
        {
            sb.append("\n" + iter.next().toString());
        }

        return sb.toString();
    }
}

