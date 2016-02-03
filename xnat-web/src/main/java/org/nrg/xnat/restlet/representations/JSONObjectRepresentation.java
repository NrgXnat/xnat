/*
 * org.nrg.xnat.restlet.representations.JSONObjectRepresentation
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.representations;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class JSONObjectRepresentation extends OutputRepresentation {
	final JSONObject o;
	
	public JSONObjectRepresentation(MediaType mediaType,final JSONObject o) {
		super(mediaType);
		this.o=o;		
	}

	@Override
	public void write(OutputStream os) throws IOException {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
			o.write(writer);
			writer.flush();
		} catch (JSONException e) {
			new IOException(e);
		}
	}

}
