/*
 * core: org.nrg.xft.exception.DBPoolException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.exception;

@SuppressWarnings("serial")
public class DBPoolException extends Exception {

	public DBPoolException()
	{
		super("Failed to create DB Pooled Connection.\nReview your InstanceSettings.xml and your Database Settings.");
	}
}

