//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Oct 19, 2004
 */
package org.nrg.xft.TypeConverter;

/**
 * @author Tim
 */
public class PGSQLMapping extends TypeMappingA {
	public PGSQLMapping(String prefix)
	{
		mapping.put("string","VARCHAR");
		mapping.put("boolean","INTEGER");
		mapping.put("float","FLOAT");
		mapping.put("double","FLOAT");
		mapping.put("decimal","DECIMAL");
		mapping.put("integer","INTEGER");
		mapping.put("nonPositiveInteger","INTEGER");
		mapping.put("negativeInteger","INTEGER");
		mapping.put("long","BIGINT");
		mapping.put("int","INTEGER");
		mapping.put("short","SMALLINT");
		mapping.put("byte","TINYINT");
		mapping.put("nonNegativeInteger","INTEGER");
		mapping.put("unsignedLong","FLOAT");
		mapping.put("unsignedInt","BIGINT");
		mapping.put("unsignedShort","INTEGER");
		mapping.put("unsignedByte","SMALLINT");
		mapping.put("positiveInteger","INTEGER");
		mapping.put("time","TIME");
		mapping.put("date","DATE");
		mapping.put("dateTime","TIMESTAMP");
		mapping.put("gYear","INTEGER");
		mapping.put("LONGVARCHAR","TEXT");
        mapping.put("anyURI","TEXT");
        mapping.put("bigserial","bigserial");
		this.setName("SQL");
	}
}

