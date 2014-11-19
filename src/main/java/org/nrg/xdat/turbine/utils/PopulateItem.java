/*
 * org.nrg.xdat.turbine.utils.PopulateItem
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.turbine.utils;
import java.util.Map;

import org.apache.turbine.util.RunData;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
/**
 * @author Tim
 *
 */
public class PopulateItem {
    private XFTItem item = null;
    private InvalidValueException error = null;
    
    /**
     * If throwException=true, then encountered errors will be thrown when they are encountered and additional properties
	 * will not be loaded.  If it is false, then the error will not be thrown until after all properties have been loaded.
     * @param data
     * @param element
     * @param throwException
     * @throws XFTInitException
     * @throws ElementNotFoundException
     * @throws FieldNotFoundException
     */
    public PopulateItem(Map<String,? extends Object> hash,UserI user, String element,boolean throwException) throws XFTInitException, ElementNotFoundException,FieldNotFoundException
    {
        item = XFTItem.NewItem(element,user);
		try {
            item.setProperties(hash,throwException);
        } catch (InvalidValueException e) {
            error = e;
        }
		item.removeEmptyItems();
    }
    /**
     * If throwException=true, then encountered errors will be thrown when they are encountered and additional properties
	 * will not be loaded.  If it is false, then the error will not be thrown until after all properties have been loaded.
     * @param data
     * @param element
     * @param throwException
     * @throws XFTInitException
     * @throws ElementNotFoundException
     * @throws FieldNotFoundException
     */
    public PopulateItem(Map<String,? extends Object> hash,UserI user, String element,boolean throwException,XFTItem newItem) throws XFTInitException, ElementNotFoundException,FieldNotFoundException
    {
        item = newItem;
		try {
            item.setProperties(hash,throwException);
        } catch (InvalidValueException e) {
            error = e;
        }
		item.removeEmptyItems();
    }
	/**
	 * If throwException=true, then encountered errors will be thrown when they are encountered and additional properties
	 * will not be loaded.  If it is false, then the error will not be thrown until after all properties have been loaded.
	 * @param data
	 * @param element
	 * @param throwException
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	public static PopulateItem Populate(Map<String,? extends Object> hash,UserI user, String element,boolean throwException) throws XFTInitException, ElementNotFoundException,FieldNotFoundException
	{
		return new PopulateItem(hash,user,element,throwException);
	}
    
	/**
	 * If throwException=true, then encountered errors will be thrown when they are encountered and additional properties
	 * will not be loaded.  If it is false, then the error will not be thrown until after all properties have been loaded.
	 * @param data
	 * @param element
	 * @param throwException
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	public static PopulateItem Populate(Map<String,? extends Object> hash,UserI user, String element,boolean throwException,XFTItem newItem) throws XFTInitException, ElementNotFoundException,FieldNotFoundException
	{
		return new PopulateItem(hash,user,element,throwException,newItem);
	}
	/**
	 * If throwException=true, then encountered errors will be thrown when they are encountered and additional properties
	 * will not be loaded.  If it is false, then the error will not be thrown until after all properties have been loaded.
	 * @param data
	 * @param element
	 * @param throwException
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	public static PopulateItem Populate(RunData data, String element,boolean throwException) throws XFTInitException, ElementNotFoundException,FieldNotFoundException
	{
		Map<String,String> hash = TurbineUtils.GetDataParameterHash(data);
		return new PopulateItem(hash,TurbineUtils.getUser(data),element,throwException);
	}
    
	/**
	 * If throwException=true, then encountered errors will be thrown when they are encountered and additional properties
	 * will not be loaded.  If it is false, then the error will not be thrown until after all properties have been loaded.
	 * @param data
	 * @param element
	 * @param throwException
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	public static PopulateItem Populate(RunData data, String element,boolean throwException,XFTItem newItem) throws XFTInitException, ElementNotFoundException,FieldNotFoundException
	{
		Map<String,String> hash = TurbineUtils.GetDataParameterHash(data);
		return new PopulateItem(hash,TurbineUtils.getUser(data),element,throwException,newItem);
	}
	
	/**
	 * @return
	 */
	public boolean hasError()
	{
	    if (error==null)
	    {
	        return false;
	    }else{
	        return true;
	    }
	}
	
    /**
     * @return Returns the error.
     */
	public InvalidValueException getError() {
        return error;
    }
    /**
     * @param error The error to set.
     */
	public void setError(InvalidValueException error) {
        this.error = error;
    }
    /**
     * @return Returns the item.
     */
	public XFTItem getItem() {
        return item;
    }
    /**
     * @param item The item to set.
     */
	public void setItem(XFTItem item) {
        this.item = item;
    }
}

