/*
 * org.nrg.xdat.om.XdatSearchField
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.om;
import java.util.Comparator;
import java.util.Hashtable;

import org.nrg.xdat.om.base.BaseXdatSearchField;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatSearchField extends BaseXdatSearchField {

	public XdatSearchField(ItemI item)
	{
		super(item);
	}

	public XdatSearchField(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatSearchField(UserI user)
	 **/
	public XdatSearchField()
	{}

	public XdatSearchField(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

    
    public static final Comparator SequenceComparator = new Comparator() {
      public int compare(Object mr1, Object mr2) throws ClassCastException {
          try{
        	Integer seq1=((XdatSearchField)mr1).getSequence();
        	Integer seq2=((XdatSearchField)mr2).getSequence();
        	
        	if(seq1==null && seq2==null){
        		return 0;
        	}else if(seq1==null){
        		return 1;
        	}else if(seq2==null){
        		return -1;
        	}
            int value1 = seq1.intValue();
            int value2 = seq2.intValue();            
    
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
