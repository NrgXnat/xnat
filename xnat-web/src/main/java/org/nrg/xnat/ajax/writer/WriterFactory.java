/*
 * org.nrg.xnat.ajax.writer.WriterFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */

package org.nrg.xnat.ajax.writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WriterFactory {
	
	public static ResponseWriterI getWriter(HttpServletRequest request, HttpServletResponse response) {
		String format = request.getParameter("format");
			if(format!=null && format.equals("xml_results")){
				return new XMLWriter(request,response);
			}else if(format!=null && format.equals("json")){
				return new JSONWriter(request,response);
			}else{
				return new HTMLWriter(request,response);
			}
	}
}	
