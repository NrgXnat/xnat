/*
 * org.nrg.util.GoogleUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import org.nrg.action.InvalidParamsException;

public class GoogleUtils {
	public static Object getFirstParam(final Multimap<String,Object> params,final String key){
		if(contains(params,key)){
			return Iterables.get(params.get(key),0);
		}else{
			return null;
		}
	}
	
	public static Object getFirstParamREQ(final Multimap<String,Object> params,final String key) throws InvalidParamsException{
		if(contains(params,key)){
			return Iterables.get(params.get(key),0);
		}else{
			throw new InvalidParamsException(key, "Required Parameter.");
		}
	}

	public static Boolean getFirstBooleanParam(final Multimap<String,Object> params,final String key,final Boolean defaultValue){
		if(contains(params,key)){
			return Boolean.parseBoolean((String)Iterables.get(params.get(key),0));
		}else{
			return defaultValue;
		}
	}
	
	public static Boolean getFirstBooleanParamREQ(final Multimap<String,Object> params,final String key) throws InvalidParamsException{
		if(contains(params,key)){
			return Boolean.parseBoolean((String)Iterables.get(params.get(key),0));
		}else{
			throw new InvalidParamsException(key, "Required Parameter.");
		}
	}
	public static boolean contains(final Multimap<String,Object> params,final String key){
		if(params.containsKey(key) && params.get(key).size()>0){
			return true;
		}else{
			return false;
		}
	}
}
