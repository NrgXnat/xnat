/*
 * core: org.nrg.xft.TypeConverter.TypeConverter
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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

