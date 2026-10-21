/*
 * web: org.nrg.xapi.configuration.RestApiConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.configuration;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.spawner.configuration.SpawnerConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Jakarta cutover note: the springfox @EnableSwagger2/Docket machinery was removed here —
 * springfox 2.9 cannot initialize on Spring 6 (dead project). The OpenAPI spec + Swagger UI are
 * now served by springdoc-openapi, wired for XNAT's non-Boot WAR in {@link OpenApiConfig}
 * (imported below). The legacy {@code io.swagger.annotations} (Swagger 1.x) on the controllers are
 * inert under springdoc — docs are generated from the Spring MVC mappings.
 */
@Configuration
@ComponentScan(value = {"org.nrg.xapi.model.users", "org.nrg.xapi.rest", "org.nrg.xnat.eventservice.rest", "org.nrg.xnat.snapshot.rest"}, includeFilters = @Filter(ControllerAdvice.class))
@Import({SpawnerConfig.class, OpenApiConfig.class})
@Slf4j
public class RestApiConfig {
}
