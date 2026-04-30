/*
 * config: org.nrg.config.exceptions.DuplicateConfigurationDetectedException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.config.exceptions;

import java.io.File;

public class DuplicateConfigurationDetectedException extends RuntimeException {
	public DuplicateConfigurationDetectedException(File file1, File file2) {
		super("The same configuration file was detected at two different locations:\n%s\nand\n%s".formatted(
               (file1 == null ? "" : file1.getAbsolutePath()),
               (file2 == null ? "" : file2.getAbsolutePath()))
		);
	}

	public DuplicateConfigurationDetectedException(String propertyName) {
		super("The configuration property '%s' was detected in two different properties files.".formatted(propertyName));
	}
}
