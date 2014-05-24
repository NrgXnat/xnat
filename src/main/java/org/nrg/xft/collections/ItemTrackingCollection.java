//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 13, 2005
 *
 */
package org.nrg.xft.collections;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
/**
 * @author Tim
 *
 */
public class ItemTrackingCollection {
	private ArrayList items = new ArrayList(); //ArrayList of Object[]{elementName,Hash of keys}
	
	public void AddItem(XFTItem x) throws XFTInitException,ElementNotFoundException
	{
		Object[] o = new Object[2];
		try {
            o[0]= x.getXSIType();
            o[1]= x.getPkValues();
            items.add(o);
        } catch (Exception e) {
        }
	}
	
	public boolean contains(XFTItem x) throws XFTInitException,ElementNotFoundException
	{
		boolean match = false;
		try {
            Hashtable pks = (Hashtable)x.getPkValues();

            Iterator iter = items.iterator();
            while (iter.hasNext())
            {
            	Object[] o = (Object[])iter.next();
            	if (((String)o[0]).equalsIgnoreCase(x.getXSIType()))
            	{
            		Hashtable storedPKS = (Hashtable)o[1];
            		Enumeration keys = pks.keys();
            		while (keys.hasMoreElements())
            		{
            			String pk = (String)keys.nextElement();
            			Object value1 = pks.get(pk);
            			Object value2 = storedPKS.get(pk);
            			if (value1 == null || value2== null)
            			{
            				match = false;
            				break;
            			}else if (value1.toString().equalsIgnoreCase(value2.toString()))
            			{
            				match = true;
            				break;
            			}
            		}
            		
            		if (match)
            		{
            			break;
            		}
            	}
            }
        } catch (Exception e) {
        }
		return match;
	}
}

