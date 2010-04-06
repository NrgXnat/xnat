//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * GENERATED FILE
 * Created on Fri May 13 14:18:33 CDT 2005
 *
 */
package org.nrg.xdat.security;
import java.util.Hashtable;
import java.util.Iterator;

import org.nrg.xdat.collections.DisplayFieldCollection.DisplayFieldNotFoundException;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.om.XdatCriteria;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplayCriteria;
import org.nrg.xdat.search.ElementCriteria;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.StringUtils;

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
