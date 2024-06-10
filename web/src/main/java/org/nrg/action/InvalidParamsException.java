/*
 * web: org.nrg.action.InvalidParamsException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.action;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class InvalidParamsException extends Exception {
	private final Multimap<String,String> mgs;
	
	public InvalidParamsException(final String param, final String msg){
		super();
		mgs=LinkedHashMultimap.create();
		mgs.put(param,msg);
	}
	
	public InvalidParamsException(final Multimap<String,String> map){
		super();
		mgs=map;
	}
	
	public Multimap<String,String> getMessages(){
		return mgs;
	}
}
