/*
 * org.nrg.xft.exception.DBPoolException
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.exception;

@SuppressWarnings("serial")
public class DBPoolException extends Exception {

	public DBPoolException()
	{
		super("Failed to create DB Pooled Connection.\nReview your InstanceSettings.xml and your Database Settings.");
	}
}

