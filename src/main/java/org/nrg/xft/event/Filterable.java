package org.nrg.xft.event;

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
	 * Include values from database.
	 *
	 * @return true, if successful
	 */
	boolean includeValuesFromDatabase() default true;  
}
