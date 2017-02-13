/*
 * web: org.nrg.xapi.configuration.RestApiConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.configuration;

import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.model.users.UserFactory;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xnat.configuration.WebConfig;
import org.nrg.xnat.services.XnatAppInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Locale;

@Configuration
@EnableSwagger2
@ComponentScan({"org.nrg.xapi.rest", "org.nrg.xnat.spawner.configuration"})
public class RestApiConfig {
    @Bean
    public UserFactory userFactory(final XdatUserAuthService service) {
        return new UserFactory(service);
    }

    @Bean
    public Docket api(final XnatAppInfo info, final MessageSource messageSource) {
        _log.debug("Initializing the Swagger Docket object");
        // TODO: When updating to Swagger 2.5.0 or later, remove the pathMapping("/xapi") call at the end.
        return new Docket(DocumentationType.SWAGGER_2).select()
                                                      .apis(RequestHandlerSelectors.withClassAnnotation(XapiRestController.class))
                                                      .paths(PathSelectors.any())
                                                      .build()
                                                      .apiInfo(apiInfo(info, messageSource))
                                                      .pathMapping("/xapi");
    }

    private ApiInfo apiInfo(final XnatAppInfo info, final MessageSource messageSource) {
        return new ApiInfo(getMessage(messageSource, "apiInfo.title"),
                           getMessage(messageSource, "apiInfo.description"),
                           info.getVersion(),
                           getMessage(messageSource, "apiInfo.termsOfServiceUrl"),
                           new Contact(getMessage(messageSource, "apiInfo.contactName"),
                                       getMessage(messageSource, "apiInfo.contactUrl"),
                                       getMessage(messageSource, "apiInfo.contactEmail")),
                           getMessage(messageSource, "apiInfo.license"),
                           getMessage(messageSource, "apiInfo.licenseUrl"));
    }

    private String getMessage(final MessageSource messageSource, final String messageId) {
        return messageSource.getMessage(messageId, null, Locale.getDefault());
    }

    private static final Logger _log = LoggerFactory.getLogger(WebConfig.class);
}
