/*
 * core: org.nrg.xft.TypeConverter.TorqueMapping
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.TypeConverter;

/**
 * @author Tim
 */
public class TorqueMapping extends TypeMappingA {
	public TorqueMapping(String prefix)
	{		
		mapping.put("string","VARCHAR");
		mapping.put("boolean","BOOLEANINT");
		mapping.put("float","FLOAT");
		mapping.put("double","DOUBLE");
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
	
		this.setName("TORQUE");
	}

}

