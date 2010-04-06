//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jun 8, 2004
 */
package org.nrg.xft.TypeConverter;
/**
 * 
 * @author Tim
 * 
 */
public class JavaMapping extends TypeMappingA {
	public JavaMapping(String prefix)
	{
		mapping.put("string", "java.lang.String");
		mapping.put("boolean", "java.lang.Boolean");
		mapping.put("float", "java.lang.Double");
		mapping.put("double", "java.lang.Double");
		mapping.put("decimal", "java.lang.Double");
		mapping.put("integer", "java.lang.Integer");
		mapping.put("nonPositiveInteger", "java.lang.Integer");
		mapping.put("negativeInteger", "java.lang.Integer");
		mapping.put("long", "java.lang.Integer");
		mapping.put("int", "java.lang.Integer");
		mapping.put("short", "java.lang.Integer");
		mapping.put("byte", "java.lang.Integer");
		mapping.put("nonNegativeInteger", "java.lang.Integer");
		mapping.put("unsignedLong", "java.lang.Double");
		mapping.put("unsignedInt", "java.lang.Integer");
		mapping.put("unsignedShort", "java.lang.Integer");
		mapping.put("unsignedByte", "java.lang.Integer");
		mapping.put("positiveInteger", "java.lang.Integer");
		mapping.put("time", "java.util.Date");
		mapping.put("date", "java.util.Date");
		mapping.put("dateTime", "java.util.Date");
		mapping.put("gYear", "java.lang.Integer");
		mapping.put("LONGVARCHAR", "java.lang.String");
		this.setName("JAVA");
	}
}
