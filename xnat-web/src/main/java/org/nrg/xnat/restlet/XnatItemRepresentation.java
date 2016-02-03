/*
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 * Released under the Simplified BSD License
 */

/**
 * XnatTableRepresentation
 * Created on 6/29/12 by Tim Olsen
 */
package org.nrg.xnat.restlet;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This indicates that a class is an XNAT table representation
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface XnatItemRepresentation {
	String mediaType();
	String mediaTypeDescription();
    boolean required() default false;
}
