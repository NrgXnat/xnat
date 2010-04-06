//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 17, 2005
 *
 */
package org.nrg.xdat.security;

import java.util.Comparator;

import org.nrg.xft.ItemI;
import org.nrg.xft.ItemWrapper;
/**
 * @author Tim
 *
 */
@SuppressWarnings("serial")
public class XDATAction extends ItemWrapper {
	public XDATAction(ItemI i)
	{
		setItem(i);
	}
	
	public String getSchemaElementName()
	{
	    return org.nrg.xft.XFT.PREFIX + ":action_type";
	}
	
	public String getName()
	{
		try {
			return (String)this.getProperty("action_name");
		} catch (Exception e) {
			return null;
		}
	}
	
	public String getDisplayName()
	{
		try {
			return (String)this.getProperty("display_name");
		} catch (Exception e) {
			return getName();
		}
	}
	
	public Integer getSequence()
	{
		try {
			return (Integer)this.getProperty("sequence");
		} catch (Exception e) {
			return new Integer(0);
		}
	}
	
	public static Comparator SequenceComparator = new Comparator() {
  	  public int compare(Object mr1, Object mr2) throws ClassCastException {
  		  try{
  			int value1 = ((XDATAction)mr1).getSequence().intValue();
  			int value2 = ((XDATAction)mr2).getSequence().intValue();

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

