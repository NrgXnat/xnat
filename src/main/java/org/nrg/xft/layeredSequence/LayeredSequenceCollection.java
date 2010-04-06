// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * Created on Apr 6, 2006
 *
 */
package org.nrg.xft.layeredSequence;


import java.util.ArrayList;

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
            for (int i=0;i<remove.size();i++)
            {
                LayeredSequenceObjectI index = (LayeredSequenceObjectI)remove.get(i);
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
        if (value1 == "" && value2 == "")
  		{
  		    return 0;
  		}else if(value1 == "")
  		{
  		    return -1;
  		}else if (value2 == "")
  		{
  		    return 1;
  		}else{
  		    if (value1.indexOf(".") ==-1 && value2.indexOf(".") ==-1)
  		    {
  		        int i1 = Integer.valueOf(value1).intValue();
  		        int i2 = Integer.valueOf(value2).intValue();
  		        
  		        return Compare(i1,i2);
  		    }else if(value1.indexOf(".") ==-1){
  		        int i1 = Integer.valueOf(value1).intValue();
  		        
  		        String first2 = value2.substring(0,value2.indexOf("."));
  		        int i2 = Integer.valueOf(first2).intValue();
  		        
  		        int compare = Compare(i1,i2);
  		        if (compare == 0)
  		        {
  		            return 2;
  		        }else{
  		            return compare;
  		        }
  		    }else if(value2.indexOf(".") ==-1){
  		      int i2 = Integer.valueOf(value2).intValue();
		        
		        String first1 = value1.substring(0,value1.indexOf("."));
		        int i1 = Integer.valueOf(first1).intValue();
		        
		        int compare = Compare(i1,i2);
		        if (compare == 0)
		        {
		            return -2;
		        }else{
		            return compare;
		        }
  		    }else{
		        String first1 = value1.substring(0,value1.indexOf("."));
		        int i1 = Integer.valueOf(first1).intValue();

  		        String first2 = value2.substring(0,value2.indexOf("."));
  		        int i2 = Integer.valueOf(first2).intValue();
  		        
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
