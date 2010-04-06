// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.identifier;

public interface IDGeneratorI {	
	public String generateIdentifier() throws Exception;
	
	public void setTable(String s);
	public String getTable();
	
	public void setDigits(Integer i);
	public Integer getDigits();
	
	public void setColumn(String s);
	public String getColumn();
	
	public void setCode(String s);
	public String getCode();
}
