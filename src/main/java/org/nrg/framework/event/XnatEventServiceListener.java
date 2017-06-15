/*
 * framework: org.nrg.framework.event.XnatEventServiceAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.event;

import java.lang.annotation.*;

/**
 * The Interface XnatEventServiceAction.
 *
 * This interface is required for all Reactor style Actions (Consumers) to be used with EventService.
 * These events can be discovered at run time and subscribed EventService Events.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XnatEventServiceListener {

    String LISTENER_CLASS = "class";
    String LISTENER_NAME = "name";
    String LISTENER_DISPLAY_NAME = "displayName";
    String LISTENER_DESC = "description";
    String LISTENER_EVENT = "event";

    /**
     * Event name (a one-word ID-like name/description).
     *
     * @return the string
     */
    String name() default "";

    String displayName() default "";

    String description() default "";

    String event() default "";

}
