/*
 * framework: org.nrg.framework.event.XnatEventServiceAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.event;

import java.lang.annotation.*;

/**
 * The Interface XnatEventServiceAction.
 *
 * This interface is required for all Reactor style Actions (Consumers) to be used with EventService.
 * These events can be discovered at run time and subscribed EventService Events.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XnatEventServiceAction {
	
	String ACTION_CLASS = "class";
	String ACTION_NAME = "name";
	String ACTION_DESC = "description";

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
