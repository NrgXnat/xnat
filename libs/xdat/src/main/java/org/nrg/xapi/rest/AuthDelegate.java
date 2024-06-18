package org.nrg.xapi.rest;

import org.nrg.xapi.authorization.XapiAuthorization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the roles permitted to access a REST method
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthDelegate {
    /**
     * Indicates a class that implements the {@link XapiAuthorization} that will handle the authorization request.
     *
     * @return The class for the authorization operation.
     */
    Class<? extends XapiAuthorization> value();
}
