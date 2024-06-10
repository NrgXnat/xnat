package org.nrg.xnat.event;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
/*
 * This point of this annotation is to avoid having to do eventBus.on(type(), this)
 * AND
 * to ensure that the event bus operates on a proxied object, so that method-level annotations work
 */
public @interface EventListener {
}
