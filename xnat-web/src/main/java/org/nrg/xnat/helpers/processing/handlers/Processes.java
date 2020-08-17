package org.nrg.xnat.helpers.processing.handlers;

import org.nrg.xnat.services.messaging.processing.ProcessingOperationRequestData;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Processes {
    Class<? extends ProcessingOperationRequestData>[] value();
}
