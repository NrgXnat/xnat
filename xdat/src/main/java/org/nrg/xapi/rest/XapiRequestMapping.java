package org.nrg.xapi.rest;

import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xdat.security.helpers.AccessLevel;
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
     * The access level required to access the annotated API method. The values {@link AccessLevel#Admin} and {@link
     * AccessLevel#Authenticated} check whether the current user is an administrator or is logged in, respectively. The
     * other values for {@link AccessLevel} include:
     *
     * <ul>
     *     <li>{@link AccessLevel#Read}</li>
     *     <li>{@link AccessLevel#Edit}</li>
     *     <li>{@link AccessLevel#Delete}</li>
     * </ul>
     *
     * These values require that the annotated method have a parameter that indicates the project being accessed. You
     * must annotate this parameter with the {@link Project} annotation. If a single project ID is specified, XNAT
     * checks that the current user has the specified access level to that project. If multiple project IDs are
     * specified, XNAT checks that the current user has the specified access level for <i>all</i> of the specified
     * projects. If the user fails the access check for <i>any</i> of the specified projects, * the {@link
     * InsufficientPrivilegesException} is thrown.
     */
    AccessLevel restrictTo() default AccessLevel.Null;
}
