/*
 * framework: org.nrg.framework.event.Filterable
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
 * The Interface Filterable.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Filterable {
	
	/**
	 * Initial values.
	 *
	 * @return the string[]
	 */
	String[] initialValues() default {};
	
	/**
	 * Default value.
	 *
	 * @return the string[]
	 */
	String defaultValue() default "";
	
	/**
	 * Is a filter value required?
	 * NOTE:  This currently only affects the UI.  This requirement is not currently enforced in the backend code.
	 * 
	 * @return true, if a filter is required
	 */
	boolean filterRequired() default false;  
	
	/**
	 * Include values from database.
	 *
	 * @return true, if successful
	 */
	boolean includeValuesFromDatabase() default true;  
}
