/*
 * framework: org.nrg.framework.event.XnatEventServiceEvent
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.event;

import java.lang.annotation.*;

/**
 * The Interface XnatEventServiceEvent.
 * 
 * This interface is required for all Reactor style events to be used with EventService.
 * These events can be discovered at run time and subscribed to by EventService Actions.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XnatEventServiceEvent {
	
	String EVENT_CLASS = "class";
	String EVENT_NAME = "name";
	String EVENT_DESC = "description";

	/**
	 * Event name (a one-word ID-like name/description).
	 *
	 * @return the string
	 */
	String name() default "";
	
	/**
	 * Event description
	 *
	 * @return the string
	 */
	String description() default "";
	
}
