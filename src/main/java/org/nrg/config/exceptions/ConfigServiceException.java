/*
 * org.nrg.config.exceptions.ConfigServiceException
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 8/26/13 6:15 PM
 */
package org.nrg.config.exceptions;

import org.nrg.config.entities.Configuration;
import org.nrg.framework.exceptions.NrgServiceException;

import java.util.ArrayList;
import java.util.List;

public class ConfigServiceException extends NrgServiceException {
	
	static final long serialVersionUID = 6690308402621986012L;
	
	public final List<Configuration> _scripts;

	public ConfigServiceException() {
        _scripts = new ArrayList<>();
    }
	
	public ConfigServiceException(String message) {
		super(message);
        _scripts = new ArrayList<>();
    }

	public ConfigServiceException(Throwable cause) {
		super(cause);
        _scripts = new ArrayList<>();
    }

	public ConfigServiceException(String message, Throwable cause){
		super(message, cause);
        _scripts = new ArrayList<>();
    }

	public ConfigServiceException(String message, List<Configuration> ss) {
		super(message);
        _scripts = new ArrayList<>();
        this._scripts.addAll(ss);
	}

	public ConfigServiceException(Throwable cause, List<Configuration> ss) {
		super(cause);
        _scripts = new ArrayList<>();
        this._scripts.addAll(ss);
	}

	public ConfigServiceException(String message, Throwable cause, List<Configuration> ss) {
		super(message, cause);
        _scripts = new ArrayList<>();
        this._scripts.addAll(ss);
	}
}
