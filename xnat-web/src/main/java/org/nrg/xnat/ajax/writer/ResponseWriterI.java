/*
 * web: org.nrg.xnat.ajax.writer.ResponseWriterI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.ajax.writer;

import org.nrg.xft.XFTTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

public interface  ResponseWriterI {
	
	public  void write(XFTTable table,  String title) throws IOException; 
	
	public   void setMetaFields(Hashtable<String, ArrayList<String>> meta) ;
}
