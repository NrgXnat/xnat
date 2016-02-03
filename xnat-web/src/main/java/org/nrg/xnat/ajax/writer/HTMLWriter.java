/*
 * org.nrg.xnat.ajax.writer.HTMLWriter
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */

package org.nrg.xnat.ajax.writer;

import org.nrg.xft.XFTTable;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Hashtable;

public class HTMLWriter implements ResponseWriterI{
	HttpServletRequest request;
	HttpServletResponse response;
	Hashtable<String, ArrayList<String>> metaFields;

	public HTMLWriter(HttpServletRequest req, HttpServletResponse resp) {
		this.request = req;
		this.response = resp;
		metaFields = new Hashtable<String,ArrayList<String>>();
	}
	
	public   void setMetaFields(Hashtable<String, ArrayList<String>> metaFields) {
	}
	
	public   void write(XFTTable table, String title) throws IOException  {
		ServletOutputStream out = response.getOutputStream();
		OutputStreamWriter sw = new OutputStreamWriter(out);
		BufferedWriter writer = new BufferedWriter(sw);
		 response.setContentType("application/xhtml+xml");
		 writer.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
		 writer.write("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">");
		 writer.write("<head><title>" + title + "</title></head><body>");
		 table.toHTML(true,writer);
		 writer.write("</body></html>");
	}
}
