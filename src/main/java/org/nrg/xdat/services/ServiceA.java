/*
 * org.nrg.xdat.services.ServiceA
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.services;

import org.apache.axis.Message;

public abstract class ServiceA {
    public String getJSessionID(Message message){
        String[]header= message.getMimeHeaders().getHeader("cookie");
        if (header!=null){
            String value = header[0];
            int start = value.indexOf("=");
            int end = value.indexOf(";");
            if (start==-1){
                return value;
            }else if(end==-1){
                return value.substring(start +1);
            }else{
                return value.substring(start +1,end);
            }
        }
        
        return null;
    }
}
