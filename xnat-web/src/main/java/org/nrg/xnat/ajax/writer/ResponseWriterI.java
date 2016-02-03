/*
 * org.nrg.xnat.ajax.writer.ResponseWriterI
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

public interface  ResponseWriterI {
	
	public  void write(XFTTable table,  String title) throws IOException; 
	
	public   void setMetaFields(Hashtable<String, ArrayList<String>> meta) ;
}
