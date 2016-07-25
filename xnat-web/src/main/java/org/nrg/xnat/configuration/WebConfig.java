package org.nrg.xnat.configuration;

import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.spawner.configuration.SpawnerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Locale;

@Configuration
@EnableWebMvc
@EnableSwagger2
@Import(SpawnerConfig.class)
@ComponentScan({"org.nrg.xapi.rest", "org.nrg.xnat.spawner.controllers"})
public class WebConfig extends WebMvcConfigurerAdapter {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("**/swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Override
    public void configurePathMatch(final PathMatchConfigurer matcher) {
        matcher.setUseRegisteredSuffixPatternMatch(true);
    }

    @Bean
    public ViewResolver viewResolver() {
        return new InternalResourceViewResolver() {{
            setViewClass(JstlView.class);
            setPrefix("/WEB-INF/views/");
            setSuffix(".jsp");
        }};
    }

    @Bean
    public MessageSource messageSource() {
        return new ReloadableResourceBundleMessageSource() {{
            setBasename("classpath:org/nrg/xnat/messages/system");
        }};
    }

    @Bean
    public Docket api(final XnatAppInfo info, final MessageSource messageSource) {
        _log.debug("Initializing the Swagger Docket object");
        return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.withClassAnnotation(XapiRestController.class)).paths(PathSelectors.any()).build().apiInfo(apiInfo(info, messageSource)).pathMapping("/xapi");
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