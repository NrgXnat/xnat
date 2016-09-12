package org.nrg.xnat.configuration;

import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.spawner.configuration.SpawnerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
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

import javax.xml.bind.Marshaller;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Configuration
@EnableWebMvc
@EnableSwagger2
@Import(SpawnerConfig.class)
@ComponentScan({"org.nrg.xapi.rest", "org.nrg.xnat.spawner.controllers"})
public class WebConfig extends WebMvcConfigurerAdapter {
    @Autowired
    public void setJackson2ObjectMapperBuilder(final Jackson2ObjectMapperBuilder objectMapperBuilder) {
        _objectMapperBuilder = objectMapperBuilder;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("**/swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter(_objectMapperBuilder.build()));
        converters.add(new MarshallingHttpMessageConverter(_marshaller, _marshaller));
        converters.add(new StringHttpMessageConverter());
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
    private static final Map<String, Object> MARSHALLER_PROPERTIES = new HashMap<String, Object>() {{ put(Marshaller.JAXB_FORMATTED_OUTPUT, true); }};

    private Jackson2ObjectMapperBuilder _objectMapperBuilder;

    private final Jaxb2Marshaller _marshaller = new Jaxb2Marshaller() {{
        setClassesToBeBound(SiteConfigPreferences.class);
        setMarshallerProperties(MARSHALLER_PROPERTIES);
    }};

}
