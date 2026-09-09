/*
 * web: org.nrg.xapi.configuration.OpenApiConfig
 * XNAT http://www.xnat.org
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.nrg.xnat.services.XnatAppInfo;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Wires springdoc-openapi into XNAT and restores the Swagger UI at {@code /xapi/swagger-ui.html}. This
 * replaces the springfox {@code Docket} that was removed during the Jakarta cutover (commit b371abc3f,
 * Phase 1-10) because springfox 2.9 cannot initialize on Spring 6.
 *
 * <p>XNAT is a plain Spring MVC WAR, not a Spring Boot application, so springdoc's Boot
 * {@code @AutoConfiguration} classes never fire. We therefore import the three config classes manually
 * (core services, the web-mvc {@code /v3/api-docs} endpoint, and the Swagger UI beans) and supply the
 * {@code @ConfigurationProperties} beans ourselves — Boot's relaxed-binding post-processor is likewise
 * absent, so the properties objects are configured programmatically here rather than from
 * {@code application.properties}. Import order is core → web-mvc → ui so the {@code @ConditionalOnMissingBean}
 * back-off (e.g. the duplicate {@code springWebProvider}) resolves the way it does under Boot.</p>
 *
 * <p>The one integration wrinkle: XNAT's {@code DispatcherServlet} is mapped at {@code /xapi/*} (see
 * {@link org.nrg.xnat.initialization.XnatWebAppInitializer}), not the root. springdoc builds every
 * generated URL — the {@code /swagger-ui.html} → {@code /swagger-ui/index.html} redirect
 * ({@code SwaggerUiHome}) and the UI's spec/config URLs ({@code SwaggerWelcomeWebMvc}) — from the context
 * path plus the {@code spring.mvc.servlet.path} property, omitting any servlet mapping it can't discover.
 * Under Boot that property is set automatically; here we publish it ({@code /xapi}) via a
 * {@link #springdocServletPath BeanFactoryPostProcessor} so it is present before springdoc's
 * {@code @Value} fields resolve. That single property makes the redirect and the spec-fetch URLs all
 * resolve under {@code /xapi} — no per-URL overrides needed. (XNAT deploys as {@code webapps/ROOT}, so
 * the context path is empty.)</p>
 *
 * <p>Operation paths still come from {@code @RequestMapping} and are servlet-relative (e.g.
 * {@code /siteConfig}), so we declare {@code /xapi} as the OpenAPI server base path — the springdoc
 * equivalent of the old {@code Docket.pathMapping("/xapi")} — so "Try it out" targets
 * {@code /xapi/siteConfig}.</p>
 */
@Configuration
@Import({
        org.springdoc.core.configuration.SpringDocConfiguration.class,
        org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration.class,
        org.springdoc.webmvc.ui.SwaggerConfig.class
})
public class OpenApiConfig {
    private static final String XAPI = "/xapi";

    /**
     * Publish XNAT's DispatcherServlet mapping to springdoc as {@code spring.mvc.servlet.path}. Registered
     * as a static {@link BeanFactoryPostProcessor} bean so the property source is added to the environment
     * before any springdoc bean resolves its {@code @Value("${spring.mvc.servlet.path:...}")} field.
     */
    @Bean
    public static BeanFactoryPostProcessor springdocServletPath(final ConfigurableEnvironment environment) {
        environment.getPropertySources()
                   .addFirst(new MapPropertySource("springdocServletPath",
                                                   Map.of("spring.mvc.servlet.path", XAPI)));
        return beanFactory -> { };
    }

    @Bean
    public SpringDocConfigProperties springDocConfigProperties() {
        return new SpringDocConfigProperties();
    }

    @Bean
    public SwaggerUiConfigProperties swaggerUiConfigProperties() {
        return new SwaggerUiConfigProperties();
    }

    @Bean
    public SwaggerUiOAuthProperties swaggerUiOAuthProperties() {
        return new SwaggerUiOAuthProperties();
    }

    @Bean
    public OpenAPI xnatOpenApi(final XnatAppInfo appInfo, final MessageSource messageSource) {
        return new OpenAPI()
                .servers(List.of(new Server().url(XAPI)))
                .info(new Info().title(getMessage(messageSource, "apiInfo.title"))
                                .description(getMessage(messageSource, "apiInfo.description"))
                                .version(appInfo.getVersion())
                                .termsOfService(getMessage(messageSource, "apiInfo.termsOfServiceUrl"))
                                .contact(new Contact().name(getMessage(messageSource, "apiInfo.contactName"))
                                                      .url(getMessage(messageSource, "apiInfo.contactUrl"))
                                                      .email(getMessage(messageSource, "apiInfo.contactEmail")))
                                .license(new License().name(getMessage(messageSource, "apiInfo.license"))
                                                      .url(getMessage(messageSource, "apiInfo.licenseUrl"))));
    }

    private static String getMessage(final MessageSource messageSource, final String messageId) {
        return messageSource.getMessage(messageId, null, Locale.getDefault());
    }
}
