//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jun 8, 2004
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

