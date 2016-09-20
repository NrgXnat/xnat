/*
 * core: org.nrg.xft.event.EventMetaI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

//Copyright 2012 Radiologics, Inc.  All Rights Reserved
package org.nrg.xft.event;

import java.util.Date;

import org.nrg.xft.security.UserI;

public interface EventMetaI {
	public String getMessage();
	public Date getEventDate();
	public String getTimestamp();
	public UserI getUser();
	public Number getEventId();
}

