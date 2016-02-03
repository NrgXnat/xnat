/*
 * org.nrg.xnat.ajax.UploadProgress
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.ajax;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

public class UploadProgress {
    public void monitor(HttpServletRequest req, HttpServletResponse response) throws IOException{
        String uploadID = req.getParameter("ID");
        //System.out.print("Monitor Progress " + uploadID + "... ");
        if (uploadID!=null)
        {
            Object upload = req.getSession().getAttribute(uploadID + "Upload");
            Object extract = req.getSession().getAttribute(uploadID + "Extract");
            ArrayList<String[]> status = (ArrayList<String[]>)req.getSession().getAttribute(uploadID + "status");
            if (upload==null)
            {
                //System.out.println("0");
                StringBuffer sb = new StringBuffer();
                sb.append("<uploadStatus upload=\"0\" extract=\"0\"/>");
                
                req.getSession().setAttribute(uploadID + "Upload",new Integer(0));
                req.getSession().setAttribute(uploadID + "Extract",new Integer(0));
                response.setContentType("text/xml");
                response.setHeader("Cache-Control", "no-cache");
                response.getWriter().write(sb.toString());
            }else{
                StringBuffer sb = new StringBuffer();
                sb.append("<uploadStatus upload=\"").append(upload);
                sb.append("\" extract=\"").append(extract);
                sb.append("\">");
                if (status !=null){
                    for(String[] s: status){
                        sb.append("<status level=\"").append(s[0]);
                        sb.append("\" message=\"").append(s[1]);
                        sb.append("\"/>");
                    }
                }
                sb.append("</uploadStatus>");
                response.setContentType("text/xml");
                response.setHeader("Cache-Control", "no-cache");
                response.getWriter().write(sb.toString());
            }
        }
    }
    
    public void start(HttpServletRequest req, HttpServletResponse response) throws IOException{
        String uploadID = req.getParameter("ID");
        if (uploadID!=null)
        {
            Object o = req.getSession().getAttribute(uploadID);
            if (o==null)
            {
                
            }else{
                StringBuffer sb = new StringBuffer();
                sb.append("<uploadStatus upload=\"0\" extract=\"0\"/>");   
                
                req.getSession().setAttribute(uploadID + "Upload",new Integer(0));
                req.getSession().setAttribute(uploadID + "Extract",new Integer(0));
                req.getSession().setAttribute(uploadID + "status",new ArrayList());
                response.setContentType("text/xml");
                response.setHeader("Cache-Control", "no-cache");
                response.getWriter().write(sb.toString());
            }
        }
    }
}
