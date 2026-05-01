/*
 * config: org.nrg.config.exceptions.ConfigServiceException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.config.exceptions;

import org.nrg.config.entities.Configuration;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;

import java.util.ArrayList;
import java.util.List;

public class ConfigServiceException extends NrgServiceException {
	public final List<Configuration> _scripts = new ArrayList<>();

	public ConfigServiceException() {
        //
    }
	
	public ConfigServiceException(final String message) {
		super(message);
    }

	public ConfigServiceException(final Throwable cause) {
		super(cause);
    }

	public ConfigServiceException(final String message, final Throwable cause){
		super(message, cause);
    }

	public ConfigServiceException(final String message, final List<Configuration> scripts) {
		super(message);
        _scripts.addAll(scripts);
	}

	public ConfigServiceException(final Throwable cause, final List<Configuration> scripts) {
		super(cause);
        _scripts.addAll(scripts);
	}

	public ConfigServiceException(final String message, final Throwable cause, final List<Configuration> scripts) {
		super(message, cause);
        _scripts.addAll(scripts);
	}

	public ConfigServiceException(final NrgServiceError error, final String message, final Throwable cause) {
		super(error, message, cause);
	}

	public ConfigServiceException(final NrgServiceError error, final String message, final Throwable cause, final List<Configuration> scripts) {
		super(error, message, cause);
		_scripts.addAll(scripts);
	}
}
