/*
 * web: org.nrg.xnat.restlet.rundata.RestletRunData
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.rundata;

import java.io.PrintWriter;
import java.util.Map;

import org.apache.turbine.services.rundata.DefaultTurbineRunData;

import com.google.common.collect.Maps;

public class RestletRunData extends DefaultTurbineRunData {
	Map<String,Object> passedObjects=Maps.newHashMap();
	
	public void passObject(String key, Object o){
		passedObjects.put(key,o);
	}
	
	public Object retrieveObject(String key){
		return passedObjects.remove(key);
	}
	
	public void hijackOutput(PrintWriter os){
		this.setOut(os);
	}
}
