/*
 * org.nrg.xdat.sortable.Sortable
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.sortable;
import java.util.Comparator;
/**
 * @author Tim
 *
 */
public abstract class Sortable {
	private int sortOrder = 0;
	
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
	
	public final static Comparator SequenceComparator = new Comparator() {
	  public int compare(Object mr1, Object mr2) throws ClassCastException {
		  try{
			int value1 = ((Sortable)mr1).getSortOrder();
			int value2 = ((Sortable)mr2).getSortOrder();

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

