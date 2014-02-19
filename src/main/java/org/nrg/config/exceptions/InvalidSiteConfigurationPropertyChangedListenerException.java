/*
 * org.nrg.config.exceptions.InvalidSiteConfigurationPropertyChangedListenerException
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 8/26/13 6:15 PM
 */
package org.nrg.config.exceptions;


public class InvalidSiteConfigurationPropertyChangedListenerException extends RuntimeException {

	public InvalidSiteConfigurationPropertyChangedListenerException(
			String message, Throwable cause) {
		super(message, cause);
	}

	private static final long serialVersionUID = -7612176965929073390L;
}
