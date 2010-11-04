/**
 * Copyright 2010 Washington University
 */
package org.nrg.xft.compare;

import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;

/**
 * @author timo
 *
 */
public abstract class ItemEqualityA implements ItemEqualityI {
	boolean allowNewNull=false, checkExtensions=true;
	
	public ItemEqualityA(final boolean allowNewNull,final boolean checkExtensions){
		this.allowNewNull=allowNewNull;
		this.checkExtensions=checkExtensions;
	}
	
	public ItemEqualityA(final boolean allowNewNull){
		this.allowNewNull=allowNewNull;
	}
	
	public ItemEqualityA(){}
	
	public abstract boolean doCheck(final XFTItem newI, final XFTItem oldI) throws XFTInitException, ElementNotFoundException, FieldNotFoundException,Exception;
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.compare.ItemEqualityI#isEqualTo(org.nrg.xft.XFTItem, org.nrg.xft.XFTItem)
	 */
	public boolean isEqualTo(final XFTItem newI, final XFTItem oldI) throws XFTInitException, ElementNotFoundException, FieldNotFoundException,Exception {
		boolean matched = false;
        if (newI.getXSIType().equalsIgnoreCase(oldI.getXSIType()))
        {
            matched =true;
        }

        if (!matched && checkExtensions)
        {
            if (newI.matchXSIType(oldI.getXSIType()) || oldI.matchXSIType(newI.getXSIType()))
            {
                matched = true;
            }
        }

        if (matched)
		{
			if ((newI.hasProperties() && !oldI.hasProperties()) || (!newI.hasProperties() && oldI.hasProperties()))
			{
				return false;
			}
			
			return this.doCheck(newI, oldI);
		}else{
			return false;
		}
	}

}
