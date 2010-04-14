//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Oct 20, 2004
 */
package org.nrg.xft.utils;
import java.util.ArrayList;
/**
 * @author Tim
 */
public class CollectionUtils {
	public static Integer FindIndexInStringArray(String[] row, String find)
	{
		Integer _return = null;
		
		for (int i=0;i<row.length;i++)
		{
			if (find.equalsIgnoreCase(row[i]))
			{
				_return = new Integer(i);
				break;
			}
		}
		
		return _return;
	}
	
	public static ArrayList ReverseOrder(ArrayList array)
	{
		ArrayList al = new ArrayList();
		
		for (int i =(array.size()-1);i>=0;i--)
		{
			al.add(array.get(i));
		}
		
		al.trimToSize();
		return al;
	}
}

