/*
 * web: org.nrg.xnat.ajax.writer.WriterFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
