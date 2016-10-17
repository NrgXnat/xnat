/*
 * core: org.nrg.xft.TypeConverter.TypeMappingA
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
	 * @return Returns the mapping
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
	 * @return Returns the type mapping name
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

