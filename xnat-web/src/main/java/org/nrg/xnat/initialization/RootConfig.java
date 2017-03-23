/*
 * web: org.nrg.xnat.initialization.RootConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import org.apache.commons.beanutils.BeanUtils;
import org.nrg.framework.beans.Beans;
import org.nrg.framework.beans.XnatPluginBeanManager;
import org.nrg.framework.datacache.SerializerRegistry;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.services.ContextService;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.configuration.ApplicationConfig;
import org.nrg.xnat.preferences.PluginOpenUrlsPreference;
import org.nrg.xnat.services.XnatAppInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration for the XNAT root application context. This contains all of the F infrastructure for initializing
 * and bootstrapping the site, including data source configuration, transaction and session management, and site
 * configuration preferences.
 * <p>
 * <b>NOTE:</b> If you are adding code to this class, please be sure you know what you're doing! Most configuration code
 * for standard XNAT components should be added in the {@link ApplicationConfig application configuration class}.
 */
@Configuration
@Import({PropertiesConfig.class, DatabaseConfig.class, SecurityConfig.class, ApplicationConfig.class, NodeConfig.class})
public class RootConfig {
    @Bean
    public XnatAppInfo appInfo(final SiteConfigPreferences preferences, final ServletContext context, final Environment environment, final SerializerService serializerService, final JdbcTemplate template, final PluginOpenUrlsPreference openUrlsPref) throws IOException {
        return new XnatAppInfo(preferences, context, environment, serializerService, template, openUrlsPref);
    }

    @Bean
    public ContextService contextService() throws NrgServiceException {
        return ContextService.getInstance();
    }

    @Bean
    public XnatPluginBeanManager xnatPluginBeanManager() {
        return new XnatPluginBeanManager();
    }

    @Bean
    public ThreadPoolExecutorFactoryBean threadPoolExecutorFactoryBean(final Path xnatHome) throws IOException, InvocationTargetException, IllegalAccessException {
        final ThreadPoolExecutorFactoryBean bean = new ThreadPoolExecutorFactoryBean();

        final Path executor = xnatHome.resolve("../executor.properties");
        if (executor.toFile().exists()) {
            try (final BufferedReader reader = Files.newBufferedReader(executor, StandardCharsets.UTF_8)) {
                final Properties properties = new Properties();
                properties.load(reader);
                final Map<String, String> converted = new HashMap<>();
                for (final String key : properties.stringPropertyNames()) {
                    converted.put(key, properties.getProperty(key));
                }
                BeanUtils.populate(bean, converted);
            }
        }

        return bean;
    }

	@Bean
    @SuppressWarnings("serial")
    public PrettyPrinter prettyPrinter() {
        return new DefaultPrettyPrinter() {
		{
            final DefaultIndenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
            indentObjectsWith(indenter);
            indentArraysWith(indenter);
        }};
    }

    @Bean
    public Jackson2ObjectMapperBuilder objectMapperBuilder() throws NrgServiceException {
        return new Jackson2ObjectMapperBuilder()
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .failOnEmptyBeans(false)
                .mixIns(mixIns())
                .featuresToEnable(JsonParser.Feature.ALLOW_SINGLE_QUOTES, JsonParser.Feature.ALLOW_YAML_COMMENTS)
                .featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS, SerializationFeature.WRITE_NULL_MAP_VALUES)
                .modulesToInstall(_jacksonModules);
    }

    @Bean
    public Module hibernateModule() {
        return new Hibernate4Module();
    }

    @Bean
    public Map<Class<?>, Class<?>> mixIns() throws NrgServiceException {
        return Beans.getMixIns();
    }

    @Bean
    public SerializerService serializerService(final Jackson2ObjectMapperBuilder objectMapperBuilder) {
        return new SerializerService(objectMapperBuilder);
    }

    @Bean
    public SerializerRegistry serializerRegistry() {
        return new SerializerRegistry();
    }

    @Autowired
    private Module[] _jacksonModules;
}
