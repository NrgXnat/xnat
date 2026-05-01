/*
 * framework: org.nrg.framework.task.XnatTask
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.task;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Interface XnatTask.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XnatTask {
	
	/** The class. */
	String CLASS = "class";
	
	/** The task id. */
	String TASK_ID = "taskId";
	
	/** The description. */
	String DESCRIPTION = "description";
	
	/** The default execution resolver. */
	String DEFAULT_EXECUTION_RESOLVER = "defaultExecutionResolver";
	
	/** The execution resolver configurable. */
	String EXECUTION_RESOLVER_CONFIGURABLE = "executionResolverConfigurable";
	
	/** The allowed execution resolvers. */
	String ALLOWED_EXECUTION_RESOLVERS = "allowedExecutionResolvers";
	
	/**
	 * Task id.
	 *
	 * @return the string
	 */
	String taskId();
	
	/**
	 * Description.
	 *
	 * @return the string
	 */
	String description();
	
	/**
	 *  
	 * resolverID for the @XnatTaskExecutionResolver used for this task.
	 *
	 * @return the string
	 */
	String defaultExecutionResolver();
	
	/**
	 *  
	 * Can administrators choose an execution resolver through the GUI?.
	 *
	 * @return true, if successful
	 */
	boolean executionResolverConfigurable() default false;
	
	/**
	 *  
	 * Optional list of allowed execution resolvers.  If this list contains values, it will be used to generate the 
	 * list of resolvers available for configuration.
	 *
	 * @return the string[]
	 */
	String[] allowedExecutionResolvers() default {};

}
