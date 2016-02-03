package org.nrg.xnat.restlet;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface XnatRestlet2 {
    XnatRestletURI[] value();
	boolean required() default false;
    boolean secure() default true;
}
