/*
 * org.nrg.xnat.services.RequestSubjectId
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.services;

import org.nrg.xdat.om.XnatSubjectdata;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map.Entry;

public class RequestSubjectId {
   private static Hashtable<String,Calendar> ids = new Hashtable<String, Calendar>();
   public String[] execute(Integer num){
       if (num.intValue()>0)
       {
           if (num.intValue()>100)
           {
               num = new Integer(100);
           }
           String[] _return = new String[num.intValue()];
           for(int i=0;i<num.intValue();i++)
           {
               String s = getID();
               _return[i]=s;
           }
           return _return;
       }else{
           return null;
       }
   }
   
   public String getID(){
       String s = null;
       boolean matched=true;
       ArrayList<String> expired = new ArrayList<String>();

       //remove cached ids from over 8 hours ago.
       Calendar expire=Calendar.getInstance();
       expire.add(Calendar.HOUR_OF_DAY, -8);
       for(Entry<String,Calendar> entry :ids.entrySet()){
           if (entry.getValue().before(expire)){
               if (!expired.contains(entry.getKey()))
               {
                   expired.add(entry.getKey());
               }
           }
        }
       
       for(String key: expired){
           ids.remove(key);
       }
       
       //generate new id
       try {
		s= XnatSubjectdata.CreateNewID();
	} catch (Exception e) {
		s="NULL";
	}
       
       ids.put(s, Calendar.getInstance());
       
       return s;
   }
}
