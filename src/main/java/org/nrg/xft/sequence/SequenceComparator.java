//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on May 6, 2005
 *
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
