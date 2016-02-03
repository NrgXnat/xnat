/*
 * org.nrg.xnat.ajax.CountFiles
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.ajax;

import org.nrg.xdat.om.XnatImagesessiondata;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class CountFiles {

    public void execute(HttpServletRequest req, HttpServletResponse response) throws IOException{
        String mrID = req.getParameter("ID");
        //System.out.print("Monitor Progress " + uploadID + "... ");
        StringBuffer sb = new StringBuffer();
        if (mrID!=null)
        {
            XnatImagesessiondata mr = (XnatImagesessiondata)XnatImagesessiondata.getXnatImagesessiondatasById(mrID, null, true);
            sb.append("<session ID=\"").append(mrID).append("\">");
            if (mr!=null){
            	try {
					Map<String,String> stats = mr.getArcFiles();
					for(Map.Entry<String,String> entry : stats.entrySet()){
					    sb.append("<fileGroup ID=\"").append(entry.getKey());
					    sb.append("\" stats=\"").append(entry.getValue()).append("\"/>");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

                
//                try {
//                    ArrayList fileNames= mr.getExtraFileNames();
//                    if (fileNames.size()>0)
//                    {
//                        String stats = mr.getArchiveStats("misc");
//                        sb.append("<scan ID=\"misc\" stats=\"").append(stats).append("\">");
//                        Iterator iter = fileNames.iterator();
//                        while (iter.hasNext())
//                        {
//                            String fileName = (String)iter.next();
//                            sb.append("<file name=\"").append(fileName).append("\"/>");
//                        }
//                        sb.append("</scan>");
//                    }
//                } catch (Exception e) {
//                }
            }
            sb.append("</session>");
            response.setContentType("text/xml");
            response.setHeader("Cache-Control", "no-cache");
            response.getWriter().write(sb.toString());
        }
    }
}
