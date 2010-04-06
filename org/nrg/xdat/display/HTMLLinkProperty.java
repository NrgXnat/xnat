//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 4, 2005
 *
 */
package org.nrg.xdat.display;

import java.util.*;
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

