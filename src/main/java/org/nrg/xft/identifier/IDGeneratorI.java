/*
 * core: org.nrg.xft.identifier.IDGeneratorI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

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
