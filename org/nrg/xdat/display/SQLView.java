//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 3, 2005
 *
 */
package org.nrg.xdat.display;

import java.util.*;
/**
 * @author Tim
 *
 */
public class SQLView {
	private String name="";
	private String sql = "";
	private int sortOrder = 0;
	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param string
	 */
	public void setSql(String string) {
		sql = string;
	}

	/**
	 * @return
	 */
	public int getSortOrder() {
		return sortOrder;
	}

	/**
	 * @param i
	 */
	public void setSortOrder(int i) {
		sortOrder = i;
	}

	public static Comparator SequenceComparator = new Comparator() {
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

