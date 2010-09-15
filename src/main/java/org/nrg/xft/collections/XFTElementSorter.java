//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 31, 2005
 *
 */
package org.nrg.xft.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
/**
 * @author Tim
 *
 */
public class XFTElementSorter {
	static org.apache.log4j.Logger logger = Logger.getLogger(XFTElementSorter.class);
	private Vector<GenericWrapperElement> elements = new Vector();
	
	private Vector unOrderedElements = new Vector();
	
	public boolean addElement(GenericWrapperElement gwe) throws XFTInitException,ElementNotFoundException
	{
		boolean inserted = false;
		ArrayList dependencies = gwe.getDependencies();
		if (dependencies.size() == 0)
		{
			elements.insertElementAt(gwe,0);
			inserted = true;
		}else{
			int counter = 0;
			Iterator iter = dependencies.iterator();
			Integer lastIndex = null;
			while (iter.hasNext())
			{
				String sqlName = (String)iter.next();
				if (!gwe.getFormattedName().equalsIgnoreCase(sqlName))
				{
					Integer index = indexOf(sqlName);
					if (index == null)
					{
						lastIndex = null;
						break;
					}else if (lastIndex == null)
					{
						lastIndex = index;
					}else if (index.intValue() > lastIndex.intValue())
					{
						lastIndex = index;
					}
				}
			}
			if (lastIndex == null)
			{
				unOrderedElements.add(gwe);
			}else{
				elements.insertElementAt(gwe,lastIndex.intValue() + 1);
				inserted = true;
			}
		}
		return inserted;
	}
	
	public Integer indexOf(String sqlName)
	{
		Iterator elementIter = elements.iterator();
		int counter = 0;
		while (elementIter.hasNext())
		{
			GenericWrapperElement gwe = (GenericWrapperElement)elementIter.next();
			if (gwe.getFormattedName().equalsIgnoreCase(sqlName))
			{
				return new Integer(counter);
			}
			counter++;
		}
		return null;
	}
	
	private void assignUnOrderedElements()throws XFTInitException,ElementNotFoundException
	{
		int added = 0;
		Vector temp = unOrderedElements;
		unOrderedElements = null;
		unOrderedElements = new Vector();
		Iterator unOrdered = temp.iterator();
		while (unOrdered.hasNext())
		{
			GenericWrapperElement gwe = (GenericWrapperElement)unOrdered.next();
			boolean result = addElement(gwe);
			if (result)
			{
				added++;
			}
		}
		
		if (added > 0 && unOrderedElements.size() > 0)
		{
			assignUnOrderedElements();
		}
	}
	
	public Vector getElements()throws XFTInitException,ElementNotFoundException,Exception
	{
		int counter = 0;
		while (unOrderedElements.size() > 0)
		{
			logger.debug("Un-ordered:" + unOrderedElements.toString());
			assignUnOrderedElements();
			if (counter++ == 10)
			{
				GenericWrapperElement gwe = (GenericWrapperElement)unOrderedElements.remove(0);
				elements.add(gwe);
				counter = 0;
			}
		}
		if (unOrderedElements.size() > 0)
		{
			Iterator unOrdered = unOrderedElements.iterator();
			String temp = "";
			while (unOrdered.hasNext())
			{
				GenericWrapperElement gwe = (GenericWrapperElement)unOrdered.next();
				temp += " - " + gwe.getFormattedName();
			}
			throw new Exception("Illegal Dependencies: " + temp);
		}
		return elements;
	}
	
	public String toString()
	{
		return "Ordered: " + elements.size() + ", un-ordered: " + unOrderedElements.size();
	}
}

