package org.nrg.xnat.initialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.nrg.framework.datacache.SerializerRegistry;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.services.ContextService;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.preferences.InitializerSiteConfiguration;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.helpers.prearchive.PrearcConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.bind.Marshaller;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for the XNAT root application context. This contains all of the basic infrastructure for initializing
 * and bootstrapping the site, including data source configuration, transaction and session management, and site
 * configuration preferences.
 *
 * <b>NOTE:</b> If you are adding code to this class, please be sure you know what you're doing! Most configuration code
 * for standard XNAT components should be added in {@link org.nrg.xnat.configuration.ApplicationConfig}
 */
@Configuration
@Import({PropertiesConfig.class, DatabaseConfig.class})
@ImportResource("WEB-INF/conf/xnat-security.xml")
public class RootConfig {
    @Bean
    public InitializerSiteConfiguration initializerSiteConfiguration() {
        return new InitializerSiteConfiguration();
    }

    @Bean
    public ContextService rootContextService() throws NrgServiceException {
        return ContextService.getInstance();
    }

    @Bean
    public PrearcConfig prearcConfig() {
        final PrearcConfig prearcConfig = new PrearcConfig();
        prearcConfig.setReloadPrearcDatabaseOnApplicationStartup(false);
        return prearcConfig;
    }

    @Bean
    public PrettyPrinter prettyPrinter() {
        final DefaultIndenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
        return new DefaultPrettyPrinter() {{
            indentObjectsWith(indenter);
            indentArraysWith(indenter);
        }};
    }

    @Bean
    public ObjectMapper jsonObjectMapper() {
        final PrettyPrinter printer = prettyPrinter();
        final ObjectMapper mapper = new ObjectMapper().setDefaultPrettyPrinter(printer);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        mapper.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
        return mapper;
    }

    @Bean
    public ObjectMapper yamlObjectMapper() {
        final PrettyPrinter printer = prettyPrinter();
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()).setDefaultPrettyPrinter(printer);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        mapper.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
        return mapper;
    }

    @Bean
    public MarshallingHttpMessageConverter marshallingMessageConverter() {
        return new MarshallingHttpMessageConverter(
                jaxb2Marshaller(),
                jaxb2Marshaller()
        );
    }

    @Bean
    public Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(SiteConfigPreferences.class);
        final Map<String, Object> marshallerProperties = new HashMap<>();
        marshallerProperties.put(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setMarshallerProperties(marshallerProperties);
        return marshaller;
    }

    @Bean
    public SerializerService serializerService() {
        return new SerializerService();
    }

    @Bean
    public SerializerRegistry serializerRegistry() {
        return new SerializerRegistry();
    }
}
