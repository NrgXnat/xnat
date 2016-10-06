/*
 * core: org.nrg.xft.identifier.IDGeneratorI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.identifier;

public interface IDGeneratorI {	
	String generateIdentifier() throws Exception;
	
	void setTable(String s);
	String getTable();
	
	void setDigits(Integer i);
	Integer getDigits();
	
	void setColumn(String s);
	String getColumn();
	
	void setCode(String s);
	String getCode();
}
