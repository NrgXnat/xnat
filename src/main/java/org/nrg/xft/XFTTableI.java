/*
 * org.nrg.xft.XFTTableI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * @author Tim
 *
 */
public interface XFTTableI {
	/**
	 * returns all columns
	 * @return
	 */
	public abstract String[] getColumns();
	/**
	 * returns number of columns
	 * @return
	 */
	public abstract int getNumCols();
	/**
	 * returns true if more rows are available
	 * @return
	 */
	public abstract boolean hasMoreRows();
	/**
	 * Returns next row and increments row cursor.
	 * @return returns next row.
	 */
	public abstract Object[] nextRow();
	public abstract Hashtable nextRowHash();
	/**
	 * If this header is found in the collection of column names, then that index
	 * is used to return the Object from the current row at that index.
	 * @param header
	 * @return
	 */
	public abstract Object getCellValue(String header);
	/**
	 * Resets row cursor to row 0
	 */
	public abstract void resetRowCursor();
	/**
	 * Outputs table headers and contents as a delimited string
	 * @param delimiter
	 * @return
	 */
	public abstract String toString(String delimiter);
	public abstract String toHTML(boolean insertTDTags,String lightColor,String darkColor,Hashtable tableProperties,int startCount);
    public abstract void toHTML(boolean insertTDTags,String lightColor,String darkColor,Hashtable tableProperties,int startCount,OutputStream out);
	/**
	 * @return
	 */
	public abstract int getNumRows();
	public abstract int size();
	public ArrayList rows();
	public ArrayList rowHashs();
	public abstract int getRowCursor();
	public Object[] removeRow(int rowNumber) throws Exception;
}

