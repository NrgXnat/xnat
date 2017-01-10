/*
 * web: org.nrg.xnat.restlet.representations.CSVTableRepresentation
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.representations;

import org.nrg.xft.XFTTable;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Hashtable;
import java.util.Map;

@SuppressWarnings("unused")
public class CSVTableRepresentation extends OutputRepresentation {
	XFTTable table = null;
	Hashtable<String,Object> tableProperties = null;
	Map<String,Map<String,String>> cp= new Hashtable<>();
	
	public CSVTableRepresentation(XFTTable table,MediaType mediaType) {
		super(mediaType);
		this.table=table;
	}
	
	public CSVTableRepresentation(XFTTable table,Hashtable<String,Object> metaFields,MediaType mediaType) {
		super(mediaType);
		this.table=table;
		this.tableProperties=metaFields;
	}
	
	public CSVTableRepresentation(XFTTable table,Map<String,Map<String,String>> columnProperties,Hashtable<String,Object> metaFields,MediaType mediaType) {
		super(mediaType);
		this.table=table;
		this.tableProperties=metaFields;
		if(columnProperties!=null)this.cp=columnProperties;
	}

	@Override
	public void write(OutputStream os) throws IOException {
		OutputStreamWriter sw = new OutputStreamWriter(os);
		BufferedWriter writer = new BufferedWriter(sw);
		table.toCSV(writer);
	    writer.flush();
	    
	}
}
