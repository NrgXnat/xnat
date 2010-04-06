//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Jun 29, 2005
 *
 */
package org.nrg.xdat.display;

import java.util.Comparator;

/**
 * @author Tim
 *
 */
public class SQLFunction {
    private String name="";
	private String content = "";

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
	public String getContent() {
		return content;
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
	public void setContent(String string) {
	    content = string;
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
			int value1 = ((SQLFunction)mr1).getSortOrder();
			int value2 = ((SQLFunction)mr2).getSortOrder();

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
