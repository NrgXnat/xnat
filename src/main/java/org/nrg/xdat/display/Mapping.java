//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 3, 2005
 *
 */
package org.nrg.xdat.display;
import java.util.ArrayList;
/**
 * @author Tim
 *
 */
public class Mapping {
	private String tableName = "";
	private ArrayList columns = new ArrayList();
	/**
	 * @return
	 */
	public ArrayList getColumns() {
		return columns;
	}

	/**
	 * @return
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @param list
	 */
	public void setColumns(ArrayList list) {
		columns = list;
	}

	/**
	 * @param string
	 */
	public void setTableName(String string) {
		tableName = string;
	}
	
	public void addColumn(MappingColumn c)
	{
		columns.add(c);
	}

}

