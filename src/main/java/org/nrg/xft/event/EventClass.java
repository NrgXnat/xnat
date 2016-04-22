package org.nrg.xft.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Interface EventClass.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventClass {
	
	/**
	 * Display name.
	 *
	 * @return the string
	 */
	String displayName();
	
	/**
	 * Default event ids.
	 *
	 * @return the string[]
	 */
	String[] defaultEventIds() default {};
	
	/**
	 * Include values from database.
	 *
	 * @return true, if successful
	 */
	boolean includeValuesFromDatabase() default true;  
}
