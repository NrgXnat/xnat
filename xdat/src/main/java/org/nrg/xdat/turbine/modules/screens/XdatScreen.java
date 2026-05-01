package org.nrg.xdat.turbine.modules.screens;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * This is a stand-in for XDAT/XNAT screen classes so that they can be marked with an annotation that indicates
 * that they're referenced without using the @SuppressWarnings annotation. Basically this does nothing but get
 * rid of the annoying unused warning in the IDE.
 */
@Documented
@Target(TYPE)
@Retention(SOURCE)
public @interface XdatScreen {
}
