/*
 * web: org.nrg.xnat.initialization.RootConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.beans.Beans;
import org.nrg.framework.beans.XnatPluginBeanManager;
import org.nrg.framework.configuration.SerializerConfig;
import org.nrg.framework.datacache.SerializerRegistry;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.node.XnatNode;
import org.nrg.framework.services.ContextService;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xft.schema.DataTypeSchemaService;
import org.nrg.xft.schema.impl.DefaultDataTypeSchemaService;
import org.nrg.xnat.configuration.ApplicationConfig;
import org.nrg.xnat.node.services.XnatNodeInfoService;
import org.nrg.xnat.preferences.PluginOpenUrlsPreference;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.services.logging.LoggingService;
import org.nrg.xnat.services.logging.impl.DefaultLoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

/**
 * Configuration for the XNAT root application context. This contains all of the F infrastructure for initializing
 * and bootstrapping the site, including data source configuration, transaction and session management, and site
 * configuration preferences.
 * <p>
 * <b>NOTE:</b> If you are adding code to this class, please be sure you know what you're doing! Most configuration code
 * for standard XNAT components should be added in the {@link ApplicationConfig application configuration class}.
 */
@Configuration
@Import({PropertiesConfig.class, DatabaseConfig.class, SecurityConfig.class, ApplicationConfig.class, NodeConfig.class, SerializerConfig.class})
@Getter(PRIVATE)
@Accessors(prefix = "_")
@Slf4j
public class RootConfig {
    @Autowired
    public void setXnatHome(final Path xnatHome) {
        log.info("Setting xnatHome to {}", xnatHome);
        _xnatHome = xnatHome;
    }

    @Autowired
    public void setJacksonModules(final Module[] jacksonModules) {
        log.info("Adding {} Jackson modules", jacksonModules != null ? jacksonModules.length : 0);
        _jacksonModules = jacksonModules;
    }

    @Bean
    public XnatAppInfo appInfo(final SiteConfigPreferences preferences, final ServletContext context, final Environment environment, final SerializerService serializerService, final JdbcTemplate template, final PluginOpenUrlsPreference openUrlsPref, final XnatNode node, final XnatNodeInfoService nodeInfoService) throws IOException {
        return new XnatAppInfo(preferences, context, environment, serializerService, template, openUrlsPref, node, nodeInfoService);
    }

    @Bean
    public ContextService contextService() {
        return ContextService.getInstance();
    }

    @Bean
    public DocumentBuilder documentBuilder() throws ParserConfigurationException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    @Bean
    public Transformer transformer() throws TransformerConfigurationException {
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        return transformer;
    }

    @Bean
    public DataTypeSchemaService dataTypeSchemaService() throws ParserConfigurationException {
        return new DefaultDataTypeSchemaService(documentBuilder());
    }

    @Bean
    public XnatPluginBeanManager xnatPluginBeanManager() {
        return new XnatPluginBeanManager();
    }

    @Bean
    public LoggingService loggingService(final Path xnatHome) throws ParserConfigurationException, SAXException, TransformerException, IOException {
        return new DefaultLoggingService(xnatHome, documentBuilder(), transformer(), xnatPluginBeanManager());
    }

    @Bean
    public PrettyPrinter prettyPrinter() {
        return new DefaultPrettyPrinter() {{
            final DefaultIndenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
            indentObjectsWith(indenter);
            indentArraysWith(indenter);
        }};
    }

    @Bean
    public Module guavaModule() {
        return new GuavaModule();
    }

    @Bean
    public ObjectMapper objectMapper(final Jackson2ObjectMapperBuilder objectMapperBuilder) {
        return objectMapperBuilder.build();
    }

    @Bean
    public Map<Class<?>, Class<?>> mixIns() throws NrgServiceException {
        return Beans.getMixIns();
    }

    @Bean
    public SerializerService serializerService(final Jackson2ObjectMapperBuilder objectMapperBuilder) {
        final SerializerConfig config = new SerializerConfig();
        try {
            return config.serializerService();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public SerializerRegistry serializerRegistry() {
        return new SerializerRegistry();
    }

    private Path     _xnatHome;
    private Module[] _jacksonModules;
}
