/*
 * web: org.nrg.xnat.configuration.WebConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.configuration;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Chars;
import org.apache.commons.lang3.ArrayUtils;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.web.converters.XftBeanHttpMessageConverter;
import org.nrg.xnat.web.converters.XftObjectHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import javax.xml.bind.Marshaller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebMvc
@EnableAspectJAutoProxy
@ComponentScan({"org.nrg.xapi.rest.aspects", "org.nrg.xapi.authorization"})
public class WebConfig extends WebMvcConfigurerAdapter {
    @Autowired
    public void setJackson2ObjectMapperBuilder(final Jackson2ObjectMapperBuilder objectMapperBuilder) {
        _objectMapperBuilder = objectMapperBuilder;
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("**/swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");

        // TODO: This is supposed to work to cache images, CSS, JS, etc., overriding the http.headers() settings in SecurityConfig (http://bit.ly/2E1i8SO),
        // TODO: but it doesn't work. This should be working so we can turn cache control on there, but override it here.
        registry.addResourceHandler("/images/**", "/pdf/**", "/resources/**", "/scripts/**", "/style/**", "/themes/**", "/favicon.ico")
                .addResourceLocations("/images/", "/pdf/", "/resources/", "/scripts/", "/style/", "/themes/", "/favicon.ico")
                .setCachePeriod(31556926);
    }

    @Override
    public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
        converters.add(stringHttpMessageConverter());
        converters.add(mappingJackson2HttpMessageConverter());
        converters.add(marshallingHttpMessageConverter());
        converters.add(resourceHttpMessageConverter());
        converters.add(xftBeanHttpMessageConverter());
        converters.add(xftObjectHttpMessageConverter());
    }

    @Override
    public void configurePathMatch(final PathMatchConfigurer matcher) {
        matcher.setUseRegisteredSuffixPatternMatch(true);
    }

    @Override
    public void configureAsyncSupport(final AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(-1);
        configurer.setTaskExecutor(asyncTaskExecutor());
    }

    @Bean
    public HttpMessageConverter<?> xftObjectHttpMessageConverter() {
        return new XftObjectHttpMessageConverter();
    }

    @Bean
    public HttpMessageConverter<?> xftBeanHttpMessageConverter() {
        return new XftBeanHttpMessageConverter();
    }

    @Bean
    public HttpMessageConverter<?> resourceHttpMessageConverter() {
        return new ResourceHttpMessageConverter();
    }

    @Bean
    public HttpMessageConverter<?> stringHttpMessageConverter() {
        return new StringHttpMessageConverter();
    }

    @Bean
    public HttpMessageConverter<?> marshallingHttpMessageConverter() {
        return new MarshallingHttpMessageConverter(_marshaller, _marshaller);
    }

    @Bean
    public HttpMessageConverter<?> mappingJackson2HttpMessageConverter() {
        final ObjectMapper objectMapper = _objectMapperBuilder.build();
        objectMapper.getFactory().setCharacterEscapes(CHARACTER_ESCAPES);
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }

    @Bean
    public AsyncTaskExecutor asyncTaskExecutor() {
        return new SimpleAsyncTaskExecutor("async");
    }

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public ViewResolver viewResolver() {
        return new InternalResourceViewResolver() {{
            setExposeContextBeansAsAttributes(true);
            setViewClass(JstlView.class);
            setPrefix("/WEB-INF/views/");
            setSuffix(".jsp");
        }};
    }

    private static final Map<String, Object> MARSHALLER_PROPERTIES = new HashMap<String, Object>() {{
        put(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    }};

    private static final CharacterEscapes CHARACTER_ESCAPES = new CharacterEscapes() {
        @Override
        public int[] getEscapeCodesForAscii() {
            final char[] allChars     = new char[]{'<', '>', '&', '\''};
            final int[]  asciiEscapes = new int[Chars.max(allChars) + 1];
            for (final char current : allChars) {
                asciiEscapes[current] = CharacterEscapes.ESCAPE_STANDARD;
            }
            return ArrayUtils.addAll(CharacterEscapes.standardAsciiEscapesForJSON(), asciiEscapes);
        }

        @Override
        public SerializableString getEscapeSequence(int ch) {
            return null;
        }
    };

    private final Jaxb2Marshaller _marshaller = new Jaxb2Marshaller() {{
        setClassesToBeBound(SiteConfigPreferences.class);
        setMarshallerProperties(MARSHALLER_PROPERTIES);
    }};

    private Jackson2ObjectMapperBuilder _objectMapperBuilder;
}
