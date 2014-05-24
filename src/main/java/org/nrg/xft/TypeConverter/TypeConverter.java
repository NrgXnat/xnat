//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Apr 1, 2004
 */
package org.nrg.xft.TypeConverter;

import org.nrg.xft.schema.XMLType;


/**
 * @author Tim
 */
public class TypeConverter {
	private TypeMappingI mapping = null;
	
	public TypeConverter(TypeMappingI map)
	{
		mapping = map;
	}
	
	public String convert(String baseType)
	{
		String temp = XMLType.CleanType(baseType);
		if (mapping.getMapping().containsKey(temp))
		{
			return (String)mapping.getMapping().get(temp);
		}else
		{
			return "";
		}
	}
	
	public String convert(String baseType, int size)
	{
		String temp = XMLType.CleanType(baseType);
		if (mapping.getMapping().containsKey(temp))
		{
			return (String)mapping.getMapping().get(temp);
		}else
		{
			return "";
		}
	}
	
	/**
	 * @return
	 */
	public String getName() {
		return mapping.getName();
	}

}

