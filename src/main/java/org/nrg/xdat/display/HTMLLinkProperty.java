/*
 * org.nrg.xdat.display.HTMLLinkProperty
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.display;

import java.util.Hashtable;
import java.util.Map;
/**
 * @author Tim
 *
 */
public class HTMLLinkProperty {
	private String name = "";
	private String value = "";
	
	private Map<String,String> insertedValues= new Hashtable<String,String>();

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public String getValue() {
		return value;
	}


	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param string
	 */
	public void setValue(String string) {
		value = string;
	}

	/**
	 * @return
	 */
	public Map<String,String> getInsertedValues() {
		return insertedValues;
	}

	/**
	 * @param hashtable
	 */
	public void setInsertedValues(Map<String,String> hashtable) {
		insertedValues = hashtable;
	}
	
	public void addInsertedValue(String id,String field)
	{
		insertedValues.put(id,field);
	}

}

