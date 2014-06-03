/*
 * org.nrg.xdat.display.Mapping
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
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

