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

