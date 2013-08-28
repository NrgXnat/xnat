/*
 * org.nrg.config.exceptions.ConfigServiceException
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 5:30 PM
 */
package org.nrg.config.exceptions;
import java.util.ArrayList;
import java.util.List;

import org.nrg.config.entities.Configuration;

public class ConfigServiceException extends Throwable {
	
	static final long serialVersionUID = 6690308402621986012L;
	
	public final List<Configuration> problem_scripts = new ArrayList<Configuration>();

	public ConfigServiceException() {
	}
	
	public ConfigServiceException(String message) {
		super(message);
	}

	public ConfigServiceException(Throwable cause) {
		super(cause);
	}

	public ConfigServiceException(String message, Throwable cause){
		super(message, cause);
	}

	public ConfigServiceException(String message, List<Configuration> ss) {
		super(message);
		this.problem_scripts.addAll(ss);
	}

	public ConfigServiceException(Throwable cause, List<Configuration> ss) {
		super(cause);
		this.problem_scripts.addAll(ss);
	}

	public ConfigServiceException(String message, Throwable cause, List<Configuration> ss) {
		super(message, cause);
		this.problem_scripts.addAll(ss);
	}
}
