/*
 * core: org.nrg.xft.layeredSequence.LayeredSequenceCollection
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.layeredSequence;


import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.collections.ItemCollection;

/**
 * @author Tim
 *
 */
public class LayeredSequenceCollection extends ItemCollection {
        
    public void addSequencedItem(LayeredSequenceObjectI o)
    {
        boolean found = false;
        ArrayList remove = new ArrayList();
        for (int i=0;i<this.size();i++)
        {
            LayeredSequenceObjectI stored = (LayeredSequenceObjectI)this.items().get(i);
            int compare= Compare(stored,o);
            if (compare ==1)
            {
                this.items().add(i,o);
                found = true;
                break;
            }else if (compare==2){
                stored.addLayeredChild(o);
                found = true;
                break;
            }else if (compare==-2){
                remove.add(stored);
                o.addLayeredChild(stored);
            }
        }
        
        if (remove.size() > 0){
            for (Object aRemove : remove) {
                LayeredSequenceObjectI index = (LayeredSequenceObjectI) aRemove;
                this.items().remove(index);
            }
        }
        
        if (!found){
            this.items().add(o);
        }
    }
    
    public static int Compare(LayeredSequenceObjectI o1, LayeredSequenceObjectI o2)
    {
        String v1 = o1.getLayeredsequence();
        String v2 = o2.getLayeredsequence();
        
        if (v1==null && v2 == null)
        {
            return 0;
        }else if (v1==null)
        {
            return -1;
        }else if (v2==null)
        {
            return 1;
        }else{
            return Compare(v1,v2);
        }
    }
    
    public static int Compare(String value1, String value2)
    {
        if (StringUtils.isEmpty(value1) && StringUtils.isEmpty(value2))
  		{
  		    return 0;
  		}else if(StringUtils.isEmpty(value1))
  		{
  		    return -1;
  		}else if (StringUtils.isEmpty(value2))
  		{
  		    return 1;
  		}else{
  		    if (!value1.contains(".") && !value2.contains("."))
  		    {
  		        int i1 = Integer.valueOf(value1);
  		        int i2 = Integer.valueOf(value2);
  		        
  		        return Compare(i1,i2);
  		    }else if(!value1.contains(".")){
  		        int i1 = Integer.valueOf(value1);
  		        
  		        String first2 = value2.substring(0,value2.indexOf("."));
  		        int i2 = Integer.valueOf(first2);
  		        
  		        int compare = Compare(i1,i2);
  		        if (compare == 0)
  		        {
  		            return 2;
  		        }else{
  		            return compare;
  		        }
  		    }else if(!value2.contains(".")){
  		      int i2 = Integer.valueOf(value2);
		        
		        String first1 = value1.substring(0,value1.indexOf("."));
		        int i1 = Integer.valueOf(first1);
		        
		        int compare = Compare(i1,i2);
		        if (compare == 0)
		        {
		            return -2;
		        }else{
		            return compare;
		        }
  		    }else{
		        String first1 = value1.substring(0,value1.indexOf("."));
		        int i1 = Integer.valueOf(first1);

  		        String first2 = value2.substring(0,value2.indexOf("."));
  		        int i2 = Integer.valueOf(first2);
  		        
  		        int compare = Compare(i1,i2);
  		        if (compare == 0)
		        {
		            return Compare(value1.substring(value1.indexOf(".")+1),value2.substring(value2.indexOf(".")+1));
		        }else{
		            return compare;
		        }
  		    }
  		}
    }
    
    public static int Compare(int i1, int i2)
    {
        if (i1 > i2)
        {
            return 1;
        }else if (i1<i2)
        {
            return -1;
        }else{
            return 0;
        }
    }
}
