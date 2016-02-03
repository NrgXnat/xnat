/*
 * org.nrg.xnat.ajax.writer.XMLWriter
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

public class XMLWriter implements ResponseWriterI {

	HttpServletRequest request;
	HttpServletResponse response;
	Hashtable<String, ArrayList<String>> metaFields;

	public XMLWriter(HttpServletRequest req, HttpServletResponse resp) {
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
		response.setContentType("text/xml");
		table.toXMLList(writer, title);
		writer.flush();
		writer.close();
	}
}
