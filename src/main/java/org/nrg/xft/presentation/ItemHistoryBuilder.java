/*
 * core: org.nrg.xft.presentation.ItemHistoryBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

//Copyright 2012 Radiologics, Inc.  All Rights Reserved
package org.nrg.xft.presentation;


public abstract class ItemHistoryBuilder {

	public boolean differBy(FlattenedItemI old, FlattenedItemI _new, String header) {
		final Object oV;
		if(old.getFields().getParams().containsKey(header)){
			oV=old.getFields().getParams().get(header);
		}else{
			oV="";
		}
		
		final Object nV;
		if(_new.getFields().getParams().containsKey(header)){
			nV=_new.getFields().getParams().get(header);
		}else{
			nV="";
		}
		if(oV.equals(nV)){
			return false;
		}else{
			return true;
		}
	}

}
