/*
 * core: org.nrg.xft.sequence.SequenceComparator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.sequence;

import java.util.Comparator;

/**
 * @author Tim
 *
 */
public class SequenceComparator {

    public static final Comparator SequenceComparator = new Comparator() {
      public int compare(Object mr1, Object mr2) throws ClassCastException {
    	  try{
    		int value1 = ((SequentialObject)mr1).getSequence();
    		int value2 = ((SequentialObject)mr2).getSequence();
    
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
