package org.nrg.automation.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to designate a class as a script runner.
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RUNTIME)
public @interface Supports {
    String value();
}
