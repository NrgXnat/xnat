/*
 * org.nrg.xdat.display.DisplayVersion
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.display;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.nrg.xdat.collections.DisplayFieldRefCollection;
import org.nrg.xdat.collections.DisplayFieldCollection.DisplayFieldNotFoundException;
/**
 * @author Tim
 *
 */
public class DisplayVersion extends DisplayFieldRefCollection{
	static org.apache.log4j.Logger logger = Logger.getLogger(DisplayVersion.class);
	private String versionName = "";
	private String defaultOrderBy = "";
	private String defaultSortOrder = "";
	private String briefDescription = "";
	private String darkColor = "";
	private String lightColor = "";
	private boolean allowDiffs = true;
	private ElementDisplay parentElementDisplay = null;
	
	private HTMLCell headerCell = new HTMLCell();
	

	/**
	 * @return
	 */
	public String getVersionName() {
		return versionName;
	}


	/**
	 * @param string
	 */
	public void setVersionName(String string) {
		versionName = string;
	}

	/**
	 * @return
	 */
	public String getDefaultOrderBy() {
		return defaultOrderBy;
	}

	/**
	 * @param string
	 */
	public void setDefaultOrderBy(String string) {
		defaultOrderBy = string;
	}

    /**
     * @return Returns the defaultSortOrder.
     */
    public String getDefaultSortOrder() {
        if (defaultSortOrder==null || defaultSortOrder.equals(""))
        {
            return "ASC";
        }else{
            return defaultSortOrder;
        }
    }
    /**
     * @param defaultSortOrder The defaultSortOrder to set.
     */
    public void setDefaultSortOrder(String defaultSortOrder) {
        this.defaultSortOrder = defaultSortOrder;
    }
    
	/**
	 * @return
	 */
	public String getBriefDescription() {
	    if (briefDescription == null || briefDescription.equals(""))
	    {
	        return this.getParentElementDisplay().getBriefDescription();
	    }else{
			return briefDescription;
	    }
	}

	/**
	 * @return
	 */
	public String getDarkColor() {
		return darkColor;
	}

	/**
	 * @return
	 */
	public String getLightColor() {
		return lightColor;
	}

	/**
	 * @param string
	 */
	public void setBriefDescription(String string) {
		briefDescription = string;
	}

	/**
	 * @param string
	 */
	public void setDarkColor(String string) {
		darkColor = string;
	}

	/**
	 * @param string
	 */
	public void setLightColor(String string) {
		lightColor = string;
	}
	
	/**
	 * Returns the visible DisplayFields
	 * @return ArrayList of DisplayFields
	 */
	public ArrayList<DisplayFieldReferenceI> getVisibleFields()
	{
		ArrayList<DisplayFieldReferenceI> al = new ArrayList<DisplayFieldReferenceI>();
		Iterator iter = this.getDisplayFieldRefIterator();
		while (iter.hasNext())
		{
			DisplayFieldRef df = (DisplayFieldRef)iter.next();
			try {
                if (df.getDisplayField().isVisible())
                {
                	al.add(df);
                }
            } catch (DisplayFieldNotFoundException e) {
                if (df.getType() != null)
	            {
                    if (df.getType().equalsIgnoreCase("COUNT"))
                    {
    	                al.add(df);
                    }
	            }else{
		            logger.error("",e);
	            }
            }
		}
		al.trimToSize();
		return al;
	}
	
	/**
	 * Returns the all defined DisplayFields
	 * @return ArrayList of DisplayFields
	 */
	public ArrayList<DisplayFieldReferenceI> getAllFields() {
		ArrayList al = new ArrayList();
		Iterator iter = this.getDisplayFieldRefIterator();
		while (iter.hasNext()) {
			DisplayFieldRef df = (DisplayFieldRef)iter.next();
                	al.add(df);
		}
		al.trimToSize();
		return al;
	}
	/**
	 * @return
	 */
	public ElementDisplay getParentElementDisplay() {
		return parentElementDisplay;
	}

	/**
	 * @param display
	 */
	public void setParentElementDisplay(ElementDisplay display) {
		parentElementDisplay = display;
	}

	/**
	 * @return
	 */
	public HTMLCell getHeaderCell() {
		return headerCell;
	}

	/**
	 * @param cell
	 */
	public void setHeaderCell(HTMLCell cell) {
		headerCell = cell;
	}

	public Collection getDisplayFields()
	{
	    ArrayList al = new ArrayList();
	    
	    Iterator iter = this.getDisplayFieldRefIterator();
	    while (iter.hasNext())
	    {
	        DisplayFieldRef ref = (DisplayFieldRef)iter.next();
	        try {
                al.add(ref.getDisplayField());
            } catch (DisplayFieldNotFoundException e) {
                logger.error("",e);
            }
	    }
	    
	    return al;
	}
    /**
     * @return Returns the allowDiffs.
     */
    public boolean isAllowDiffs() {
        return allowDiffs;
    }
    /**
     * @param allowDiffs The allowDiffs to set.
     */
    public void setAllowDiffs(boolean allowDiffs) {
        this.allowDiffs = allowDiffs;
    }
}

