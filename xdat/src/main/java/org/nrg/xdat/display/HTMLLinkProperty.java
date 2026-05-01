/*
 * core: org.nrg.xdat.display.HTMLLinkProperty
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.display;

import java.util.Hashtable;
import java.util.Map;
/**
 * @author Tim
 */
public class HTMLLinkProperty {
	private String name = "";
	private String value = "";
	
	private Map<String,String> insertedValues= new Hashtable<>();

	/**
	 * @return The property's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The property's value.
	 */
	public String getValue() {
		return value;
	}


	/**
	 * @param string    The name to set for the property.
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param string    The value to set for the property.
	 */
	public void setValue(String string) {
		value = string;
	}

	/**
	 * @return A map of the inserted name/value pairs.
	 */
	public Map<String,String> getInsertedValues() {
		return insertedValues;
	}

	public void addInsertedValue(String id,String field)
	{
		insertedValues.put(id,field);
	}

}

