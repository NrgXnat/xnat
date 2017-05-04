package org.nrg.xapi.rest;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies a method within an XAPI REST controller. This annotation is, for all practical purposes, the same as the
 * standard Spring Framework {@link RequestMapping} annotation, with the same annotation attributes, which are passed
 * along to the underlying annotation processing. This annotation adds XAPI-specific functionality to the annotated
 * methods, including access logging.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping
public @interface XapiRequestMapping {
    /**
     * Maps to the {@link RequestMapping#name()} attribute.
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "name")
    String name() default "";

    /**
     * Maps to the {@link RequestMapping#value()} attribute.
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "value")
    String[] value() default {};

    /**
     * Maps to the {@link RequestMapping#path()} attribute.
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "path")
    String[] path() default {};

    /**
     * Maps to the {@link RequestMapping#method()} attribute.
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "method")
    RequestMethod[] method() default {};

    /**
     * Maps to the {@link RequestMapping#params()} attribute.
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "params")
    String[] params() default {};

    /**
     * Maps to the {@link RequestMapping#headers()} attribute.
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "headers")
    String[] headers() default {};

    /**
     * Maps to the {@link RequestMapping#consumes()} attribute.
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "consumes")
    String[] consumes() default {};

    /**
     * Maps to the {@link RequestMapping#produces()} attribute.
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "produces")
    String[] produces() default {};

    /**
     * The level of project access required to access the annotated API method. This requires that the method have a
     * parameter named <b>projectId</b> that indicates the project being accessed. Valid values for this attribute
     * include "admin, "authenticated", "read", "edit", and "owner". Note that specifying the latter three values
     * requires including a String named <b>projectId</b> in the method parameters.
     */
    String restrictTo() default "";
}
