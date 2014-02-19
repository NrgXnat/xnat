/*
 * org.nrg.xdat.security.XdatCriteriaSet
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.security;
import java.util.Hashtable;
import java.util.Iterator;

import org.nrg.xdat.om.XdatCriteria;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatCriteriaSet extends org.nrg.xdat.om.XdatCriteriaSet {

	public XdatCriteriaSet(ItemI item)
	{
		super(item);
	}

	public XdatCriteriaSet(UserI user)
	{
		super(user);
	}

	public XdatCriteriaSet(Hashtable properties,UserI user)
	{
		super(properties,user);
	}
	


	public CriteriaCollection getDisplaySearchCriteria() throws Exception
	{
		final CriteriaCollection cc = new CriteriaCollection(this.getMethod());

	    Iterator iter = getChildItems("xdat:criteria_set.criteria").iterator();
	    while (iter.hasNext())
	    {
	    	final XFTItem child = (XFTItem)iter.next();
	    	final XdatCriteria c = new XdatCriteria(child);
	    	final  String custom_search = c.getCustomSearch();
	        if (custom_search == null || custom_search.equals(""))
	        {
	            cc.add(c.buildDisplaySearchCriteria());
	        }else{

	        }
	    }

	    iter = getChildItems("xdat:criteria_set.child_set").iterator();
	    while (iter.hasNext())
	    {
	        XFTItem child = (XFTItem)iter.next();
	        XdatCriteriaSet set = new XdatCriteriaSet(child);
	        cc.add(set.getDisplaySearchCriteria());
	    }

	    return cc;
	}	

	public CriteriaCollection getItemSearchCriteria() throws Exception
	{
	    final CriteriaCollection cc = new CriteriaCollection(this.getMethod());

	    Iterator iter = getChildItems("xdat:criteria_set.criteria").iterator();
	    while (iter.hasNext())
	    {
	    	final XFTItem child = (XFTItem)iter.next();
	    	final XdatCriteria c = new XdatCriteria(child);
	    	final String custom_search = c.getCustomSearch();
	        if (custom_search == null || custom_search.equals(""))
	        {
	            cc.add(c.buildItemSearchCriteria());
	        }else{

	        }
	    }

	    iter = getChildItems("xdat:criteria_set.child_set").iterator();
	    while (iter.hasNext())
	    {
	    	final XFTItem child = (XFTItem)iter.next();
	    	final XdatCriteriaSet set = new XdatCriteriaSet(child);
	        cc.add(set.getItemSearchCriteria());
	    }

	    return cc;
	}
	
	public boolean hasInClause(){
		try {
			Iterator iter = getChildItems("xdat:criteria_set.criteria").iterator();
			while (iter.hasNext())
			{
				final XFTItem child = (XFTItem)iter.next();
				final XdatCriteria c = new XdatCriteria(child);
			    if(c.getComparisonType().equals("IN"))return true;
			}
		} catch (XFTInitException e) {
			logger.error(e);
		} catch (ElementNotFoundException e) {
			logger.error(e);
		} catch (FieldNotFoundException e) {
			logger.error(e);
		}
	    return false;
	}
}
