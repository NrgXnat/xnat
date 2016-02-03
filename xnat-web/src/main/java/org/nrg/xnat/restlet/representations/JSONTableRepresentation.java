/*
 * org.nrg.xnat.restlet.representations.JSONTableRepresentation
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.representations;

import org.nrg.xft.XFTTable;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

public class JSONTableRepresentation extends OutputRepresentation {
	XFTTable table = null;
	Hashtable<String, Object> tableProperties = null;
	Map<String,Map<String,String>> cp=new Hashtable<String,Map<String,String>>();
	
	public JSONTableRepresentation(XFTTable table,MediaType mediaType) {
		super(mediaType);
		this.table=table;
	}
	
	public JSONTableRepresentation(XFTTable table,Hashtable<String, Object> metaFields,MediaType mediaType) {
		super(mediaType);
		this.table=table;
		this.tableProperties=metaFields;
	}

	public JSONTableRepresentation(XFTTable table,Map<String,Map<String,String>> columnProperties,Hashtable<String,Object> metaFields,MediaType mediaType) {
		super(mediaType);
		this.table=table;
		this.tableProperties=metaFields;
		if(columnProperties!=null)this.cp=columnProperties; 
	}

	@Override
	public void write(OutputStream os) throws IOException {
		OutputStreamWriter sw = new OutputStreamWriter(os);
		BufferedWriter writer = new BufferedWriter(sw);

	    writer.write("{\"ResultSet\":{");
	    if(cp.size()>0){
		    writer.write("\"Columns\":[");
		    int columnCounter=0;
	    	for(Map.Entry<String,Map<String,String>> column: cp.entrySet()){
	    		if(columnCounter++>0)writer.write(",");
			    writer.write("{\"key\":\"" + column.getKey()+ "\"");
	    		for(Map.Entry<String,String> prop:column.getValue().entrySet()){
	    			writer.write(",\"" + prop.getKey()+ "\":");
	    			if(!(prop.getValue().startsWith("{") || prop.getValue().startsWith("[") ))
	    				writer.write("\"");
	    			writer.write("" + prop.getValue());
	    			if(!(prop.getValue().startsWith("{") || prop.getValue().startsWith("[") ))
	    				writer.write("\"");
	    		}
			    writer.write("}");
	    	}

		    writer.write("],");
	    }
	    writer.write("\"Result\":");
		table.toJSON(writer,cp);
		if (tableProperties != null  && tableProperties.size() > 0) {
			writer.write(", ");
			String appendMeta = "";
			Enumeration<String> keys = tableProperties.keys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				Object value = tableProperties.get(key);
				if (value != null) {
					if(value instanceof String && ((String)value).startsWith("{")){
						appendMeta += "\"" + key + "\": " ;
						appendMeta += value;
						appendMeta += ",";
					}else{
					appendMeta += "\"" + key + "\": " ;
					appendMeta += flattenValue(value);
					appendMeta += ",";
					}
				}
			}
			if (appendMeta.endsWith(",")) {
				int i = appendMeta.lastIndexOf(",");
				if (i != -1) {
					appendMeta = appendMeta.substring(0, i);
				}
			}
			writer.write(appendMeta);
		}
		writer.write("}}");
		writer.flush();
	}
	
	private String flattenValue(Object v) {
		if (v == null) return "\"\"";
		if(v instanceof ArrayList){
			@SuppressWarnings("unchecked")
			ArrayList<String> values = (ArrayList<String>)v;
			if (values.size() == 1) {
				String rtn = "\"" + values.get(0) + "\"";
				return rtn;
			}
			int lastIndex = values.size() - 1 ; 
			String rtn = "{\"" + values.get(lastIndex - 1) + "\":\"" + values.get(lastIndex) + "\"}";
			for (int i = lastIndex - 2 ; i >= 0; i--) {
				rtn = "{\""  + values.get(i) + "\":" + rtn + "}";
			}
			return rtn;
		}else{
			String rtn = "\"" + v.toString() + "\"";
			return rtn;
		}
	}

}
