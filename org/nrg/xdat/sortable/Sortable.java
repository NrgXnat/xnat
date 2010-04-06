//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 17, 2005
 *
 */
package org.nrg.xdat.sortable;
import java.util.*;
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
	
	public static Comparator SequenceComparator = new Comparator() {
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

