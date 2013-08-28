/*
 * org.nrg.config.exceptions.InvalidSiteConfigurationPropertyChangedListenerException
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 5:30 PM
 */
package org.nrg.config.exceptions;


public class InvalidSiteConfigurationPropertyChangedListenerException extends RuntimeException {

	public InvalidSiteConfigurationPropertyChangedListenerException(
			String message, Throwable cause) {
		super(message, cause);
	}

	private static final long serialVersionUID = -7612176965929073390L;
}
