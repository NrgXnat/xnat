/*
 * web: org.nrg.xnat.restlet.util.FileWriterWrapperI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface FileWriterWrapperI {
	static enum UPLOAD_TYPE{INBODY,MULTIPART,OTHER};

	void write(File f) throws Exception;

	String getName();

    String getNestedPath();

	InputStream getInputStream() throws IOException;

	void delete();

	UPLOAD_TYPE getType();

}
