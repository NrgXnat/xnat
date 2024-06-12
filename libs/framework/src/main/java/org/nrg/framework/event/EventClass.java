/*
 * framework: org.nrg.framework.event.EventClass
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Interface EventClass.  
 * 
 * This interface is not required for all events used in XNAT.  This is currently used for AutomationEventImplementerI 
 * events to populate the upload UI.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventClass {
	
	String EVENT_CLASS = "class";
	String EVENT_NAME = "name";
	String EVENT_DESC = "description";
	String EVENT_DEFAULTIDS = "defaultEventIds";
	String EVENT_INCLUDEFROMDATABASE = "includeValuesFromDatabase";
	
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
	
	/**
	 * Default event ids.
	 *
	 * @return the string[]
	 */
	String[] defaultEventIds() default {};
	
	/**
	 * Include event ID values from database.
	 *
	 * @return true, if successful
	 */
	boolean includeValuesFromDatabase() default true;  
}
