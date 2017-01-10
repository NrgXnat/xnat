/*
 * core: org.nrg.xft.compare.ItemPKEquality
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.compare;

import java.util.Map;

import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;

/**
 * @author timo
 *
 */
public class ItemPKEquality extends ItemEqualityA implements ItemEqualityI{
	
	public ItemPKEquality(final boolean allowNewNull,final boolean checkExtensions){
		super(allowNewNull,checkExtensions);
	}
	
	public ItemPKEquality(final boolean allowNewNull){
		super(allowNewNull);
	}
	
	public ItemPKEquality(){
		super();
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.compare.ItemEqualityA#doCheck(org.nrg.xft.XFTItem, org.nrg.xft.XFTItem)
	 */
	public boolean doCheck(final XFTItem newI, final XFTItem oldI) throws XFTInitException, ElementNotFoundException, FieldNotFoundException,Exception{
		final Map<String,Object> pks = newI.getPkValues();
		if (pks.size() > 0)
		{
			boolean match = true;
			for(Map.Entry<String,Object> entry:pks.entrySet()){
				final String key = entry.getKey();
				final Object newItemKey = newI.getProperty(newI.getXSIType() + XFT.PATH_SEPARATOR + key);
				final Object oldItemKey = oldI.getProperty(oldI.getXSIType() + XFT.PATH_SEPARATOR + key);
				if (oldItemKey == null)
				{
				    throw new NullPointerException("NULL PRIMARY KEY");
				}else if (allowNewNull)
				{
					if (newItemKey== null)
					{
						match = true;
					}else{
						if (oldItemKey.equals(newItemKey))
						{
							match = true;
						}else
						{
							match = false;
							break;
						}
					}
				}else{
					if (oldItemKey.equals(newItemKey))
					{
						match = true;
					}else
					{
						match = false;
						break;
					}
				}
			}
			
			return match;
		}else
		{
			return false;
		}
	}
}
