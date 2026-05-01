/*
 * DicomEdit: IHttpClient
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.service.http;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public interface IHttpClient {
	default List<String> requestAsList(URL url) throws IOException {
		throw new UnsupportedOperationException();
	}
	String request(URL url) throws IOException;
}
