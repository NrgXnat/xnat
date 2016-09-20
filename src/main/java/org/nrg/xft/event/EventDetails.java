/*
 * core: org.nrg.xft.event.EventDetails
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

//Copyright 2012 Radiologics, Inc.  All Rights Reserved
package org.nrg.xft.event;

import org.nrg.xft.event.EventUtils.CATEGORY;
import org.nrg.xft.event.EventUtils.TYPE;

public class EventDetails {
	EventUtils.CATEGORY category;
	EventUtils.TYPE type;
	String action;
	String reason;
	String comment;
	
	public EventDetails(CATEGORY category, TYPE type, String action, String reason, String comment) {
		super();
		this.category = category;
		this.type = type;
		this.action = action;
		this.reason = reason;
		this.comment = comment;
	}
	
	public EventDetails() {
		super();
	}

	public EventUtils.CATEGORY getCategory() {
		return category;
	}

	public void setCategory(EventUtils.CATEGORY category) {
		this.category = category;
	}

	public EventUtils.TYPE getType() {
		return type;
	}

	public void setType(EventUtils.TYPE type) {
		this.type = type;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
