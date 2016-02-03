/*
 * org.nrg.xnat.restlet.representations.XMLXFTItemRepresentation
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.representations;

import org.nrg.xft.XFTItem;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXWriter;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Hashtable;
import java.util.Map;


public class XMLXFTItemRepresentation extends OutputRepresentation {
	XFTItem item;
	Hashtable<String,Object> metaFields;
	boolean allowDBAccess;
	boolean allowSchemaLocation;
	
	public XMLXFTItemRepresentation(XFTItem item,MediaType mediaType, Hashtable<String,Object> metaFields, boolean allowDBAccess, boolean allowSchemaLocation) {
		super(mediaType);
		this.item = item;
		this.allowDBAccess = allowDBAccess;
		this.metaFields = metaFields;
		this.allowSchemaLocation = allowSchemaLocation;
	}
	
	@Override
	public void write(OutputStream os) throws IOException {
		OutputStreamWriter sw = new OutputStreamWriter(os);
		BufferedWriter writer = new BufferedWriter(sw);
		if (!allowSchemaLocation && metaFields!=null && metaFields.size()>0) {
			writer.write("<ResultSet");
			if(metaFields!=null && metaFields.size()>0){
				for(Map.Entry<String,Object> entry : this.metaFields.entrySet()){
					writer.write(" " + entry.getKey() + "=\"");
					writer.write(entry.getValue().toString());
					writer.write("\"");
				}
			}
			writer.write(">");
		}
		try {
            SAXWriter swriter = new SAXWriter(writer,allowDBAccess);
            swriter.setAllowSchemaLocation(allowSchemaLocation);
            swriter.write(item);
		}catch(Exception saxe) {
			saxe.printStackTrace();
			throw new IOException("Encountered Exception " + saxe.getClass());
		}finally {
			if (!allowSchemaLocation && metaFields!=null && metaFields.size()>0)  writer.write("</ResultSet>");
			writer.flush();
		}
	}
}
