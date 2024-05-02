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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.micrometer.web.handler.XnatMicrometerHandlerInterceptorAdapter;
import org.nrg.xnat.preferences.AsyncOperationsPreferences;
import org.nrg.xnat.web.converters.XftBeanHttpMessageConverter;
import org.nrg.xnat.web.converters.XftObjectHttpMessageConverter;
import org.nrg.xnat.web.converters.ZipFileHttpMessageConverter;
import org.nrg.xnat.web.http.AsyncLifecycleMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Slf4j
@ComponentScan({"org.nrg.xapi.rest.aspects", "org.nrg.xapi.authorization", "org.nrg.xapi.pages", "org.nrg.xnat.micrometer.web.handler"})
public class WebConfig extends WebMvcConfigurerAdapter {

    @Autowired
    public WebConfig(final Jackson2ObjectMapperBuilder objectMapperBuilder,
                     @Qualifier("threadPoolExecutorFactoryBean") final ThreadPoolExecutorFactoryBean threadPoolExecutorFactoryBean,
                     final AsyncOperationsPreferences preferences,
                     final XnatMicrometerHandlerInterceptorAdapter handlerInterceptorAdapter) {
        _threadPoolFactory = threadPoolExecutorFactoryBean;
        _preferences = preferences;
        _objectMapper = objectMapperBuilder.build();
        _objectMapper.getFactory().setCharacterEscapes(CHARACTER_ESCAPES);
        _marshaller = new Jaxb2Marshaller();
        _marshaller.setClassesToBeBound(SiteConfigPreferences.class);
        _marshaller.setMarshallerProperties(MARSHALLER_PROPERTIES);
        _xnatHandlerInterceptorAdapter = handlerInterceptorAdapter;
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
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(_xnatHandlerInterceptorAdapter)
                .addPathPatterns("/**")
                .order(0);
    }

    @Override
    public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
        converters.add(stringHttpMessageConverter());
        converters.add(mappingJackson2HttpMessageConverter());
        converters.add(marshallingHttpMessageConverter());
        converters.add(resourceHttpMessageConverter());
        converters.add(xftBeanHttpMessageConverter());
        converters.add(xftObjectHttpMessageConverter());
        converters.add(zipFileHttpMessageConverter());
    }

    @Override
    public void configurePathMatch(final PathMatchConfigurer matcher) {
        matcher.setUseRegisteredSuffixPatternMatch(true);
    }

    @Override
    public void configureAsyncSupport(final AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(_preferences.getDefaultTimeout());
        configurer.setTaskExecutor(asyncTaskExecutor());
        configurer.registerCallableInterceptors(new AsyncLifecycleMonitor());
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
    public HttpMessageConverter<?> zipFileHttpMessageConverter() {
        return new ZipFileHttpMessageConverter();
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
        return new MappingJackson2HttpMessageConverter(_objectMapper);
    }

    @Bean
    public AsyncTaskExecutor asyncTaskExecutor() {
        final int     corePoolSize           = _preferences.getCorePoolSize();
        final boolean allowCoreThreadTimeOut = _preferences.getAllowCoreThreadTimeOut();
        final int     maxPoolSize            = _preferences.getMaxPoolSize();
        final int     keepAliveSeconds       = _preferences.getKeepAliveSeconds();

        log.info("Configuring async task executor with core pool size {}, max pool size {}, keep-alive seconds {}, and allow core thread timeout {}", corePoolSize, maxPoolSize, keepAliveSeconds, allowCoreThreadTimeOut);
        final ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadFactory(_threadPoolFactory);
        taskExecutor.setCorePoolSize(corePoolSize);
        taskExecutor.setAllowCoreThreadTimeOut(allowCoreThreadTimeOut);
        taskExecutor.setMaxPoolSize(maxPoolSize);
        taskExecutor.setKeepAliveSeconds(keepAliveSeconds);
        return taskExecutor;
    }

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public ViewResolver viewResolver() {
        final InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setExposeContextBeansAsAttributes(true);
        resolver.setViewClass(JstlView.class);
        resolver.setPrefix("/page/");
        return resolver;
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

    private final AsyncOperationsPreferences    _preferences;
    private final Jaxb2Marshaller               _marshaller;
    private final ThreadPoolExecutorFactoryBean _threadPoolFactory;
    private       ObjectMapper                  _objectMapper;
    private final XnatMicrometerHandlerInterceptorAdapter _xnatHandlerInterceptorAdapter;

}
