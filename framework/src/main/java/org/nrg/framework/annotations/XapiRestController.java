/*
 * framework: org.nrg.framework.annotations.XapiRestController
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.annotations;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

/**
 * Annotation to support dynamic location of REST controllers within the XNAT XAPI framework. This not only marks the
 * class as a standard {@link RestController Spring REST controller}, but includes the API in the XNAT Swagger API
 * listings.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
@ResponseBody
public @interface XapiRestController {
    String value() default "";
}
