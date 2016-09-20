/*
 * core: org.nrg.xdat.display.SQLView
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.display;

import java.util.Comparator;
/**
 * @author Tim
 *
 */
public class SQLView {
	private String name="";
	private String sql = "";
	private int sortOrder = 0;
	/**
	 * @return The SQl view name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The SQl for the view.
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * @param string    The name to set for the SQL view.
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param string    The SQL to set for the view.
	 */
	public void setSql(String string) {
		sql = string;
	}

	/**
	 * @return The sort order for the view.
	 */
	public int getSortOrder() {
		return sortOrder;
	}

	/**
	 * @param i    The sort order to set for the view.
	 */
	public void setSortOrder(int i) {
		sortOrder = i;
	}

	public final static Comparator SequenceComparator = new Comparator() {
	  public int compare(Object mr1, Object mr2) throws ClassCastException {
		  try{
			int value1 = ((SQLView)mr1).getSortOrder();
			int value2 = ((SQLView)mr2).getSortOrder();

			if (value1 > value2)
			  {
				  return 1;
			  }else if(value1 < value2)
			  {
				  return -1;
			  }else
			  {
				  return 0;
			  }
		  }catch(Exception ex)
		  {
			  throw new ClassCastException("Error Comparing Sequence");
		  }
	  }
	};
}

