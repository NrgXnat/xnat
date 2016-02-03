/*
 * org.nrg.xnat.restlet.representations.CatalogRepresentation
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.representations;

import org.apache.log4j.Logger;
import org.nrg.xdat.bean.CatCatalogBean;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

import javax.xml.transform.TransformerFactoryConfigurationError;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

@Deprecated
public class CatalogRepresentation extends OutputRepresentation {
	static org.apache.log4j.Logger logger = Logger.getLogger(ItemXMLRepresentation.class);
	CatCatalogBean cat = null;
	boolean includeSchemaLocations=true;
	
	public CatalogRepresentation(CatCatalogBean i,MediaType mt,boolean includeSchemaLocations) {
		super(mt);
		cat=i;	
		this.includeSchemaLocations=includeSchemaLocations;
	}

	public CatalogRepresentation(CatCatalogBean i,MediaType mt) {
		super(mt);
		cat=i;	
	}
	
	@Override
	public void write(OutputStream out) throws IOException {
			try {
				PrintWriter pw = new PrintWriter(out);
				cat.toXML(pw, false);
				pw.close();
			} catch (IllegalArgumentException e) {
				logger.error("",e);
			} catch (TransformerFactoryConfigurationError e) {
				logger.error("",e);
			}
	}

	
}
