package org.nrg.config.exceptions;

import java.io.File;

public class DuplicateConfigurationDetectedException extends RuntimeException {

	private static final long serialVersionUID = -7612176965929073390L;

	public DuplicateConfigurationDetectedException(File file1, File file2) {
		super(String.format("The same configuration file was detected at two different locations:\n%s\nand\n%s", 
						(file1 == null ? "" : file1.getAbsolutePath()), 
						(file2 == null ? "" : file2.getAbsolutePath()))
		);
	}

	public DuplicateConfigurationDetectedException(String propertyName) {
		super(String.format("The configuration property '%s' was detected either in two different properties files.", propertyName)); 
	}
}
