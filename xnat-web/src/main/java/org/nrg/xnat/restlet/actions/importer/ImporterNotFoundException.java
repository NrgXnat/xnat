/*
 * web: org.nrg.xnat.restlet.actions.importer.ImporterNotFoundException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.actions.importer;

import java.io.Serial;

public class ImporterNotFoundException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

	public ImporterNotFoundException(String string,
			IllegalArgumentException illegalArgumentException) {
		super(string,illegalArgumentException);
	}

}
