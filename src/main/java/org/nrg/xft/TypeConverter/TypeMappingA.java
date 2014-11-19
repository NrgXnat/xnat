/*
 * org.nrg.xft.TypeConverter.TypeMappingA
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.TypeConverter;
import java.util.Hashtable;

/**
 * @author Tim
 */
public abstract class TypeMappingA implements TypeMappingI {
	public Hashtable mapping = new Hashtable();
	private String name = "";

	/**
	 * @return
	 */
	public Hashtable getMapping() {
		return mapping;
	}

	/**
	 * @param hashtable
	 */
	public void setMapping(Hashtable hashtable) {
		mapping = hashtable;
	}
	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

}

