//Copyright 2007 Washington University School of Medicine All Rights Reserved
/*
 * Created on Oct 1, 2007
 *
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
