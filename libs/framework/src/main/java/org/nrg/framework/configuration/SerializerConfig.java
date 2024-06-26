package org.nrg.framework.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.beans.Beans;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.services.SerializerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("DuplicatedCode")
@Configuration
@Slf4j
@Getter(AccessLevel.PRIVATE)
@Accessors(prefix = "_")
public class SerializerConfig {
    @Autowired
    public void setJacksonModules(final Module[] jacksonModules) {
        log.info("Adding {} Jackson modules", jacksonModules != null ? jacksonModules.length : 0);
        if (jacksonModules != null) {
            _jacksonModules.addAll(Arrays.asList(jacksonModules));
        }
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
    public Jackson2ObjectMapperBuilder objectMapperBuilder() throws NrgServiceException {
        return new Jackson2ObjectMapperBuilder()
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .failOnEmptyBeans(false)
                .mixIns(mixIns())
                .featuresToEnable(JsonParser.Feature.ALLOW_SINGLE_QUOTES, JsonParser.Feature.ALLOW_YAML_COMMENTS)
                .featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .modulesToInstall(_jacksonModules.toArray(new Module[0]));
    }

    @Bean
    public DocumentBuilderFactory documentBuilderFactory() throws NrgServiceException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setExpandEntityReferences(false);
            factory.setXIncludeAware(false);
            factory.setNamespaceAware(true);
            return factory;
        } catch (ParserConfigurationException e) {
            throw new NrgServiceException("Failed to set 'Secure Processing' feature on DocumentBuilderFactory implementation of type " + factory.getClass(), e);
        }
    }

    @Bean
    public SAXParserFactory saxParserFactory() throws NrgServiceException {
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setNamespaceAware(true);
            return factory;
        } catch (SAXNotRecognizedException | ParserConfigurationException | SAXNotSupportedException e) {
            throw new NrgServiceException("Failed to set 'Secure Processing' feature on SAXParserFactory implementation of type " + factory.getClass(), e);
        }
    }

    @Bean
    public TransformerFactory transformerFactory() throws NrgServiceException {
        final TransformerFactory factory = TransformerFactory.newInstance();
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            setAttributes(factory, XMLConstants.ACCESS_EXTERNAL_DTD, XMLConstants.ACCESS_EXTERNAL_STYLESHEET);
            return factory;
        } catch (TransformerConfigurationException e) {
            throw new NrgServiceException("Failed to set 'Secure Processing' feature on TransformerFactory implementation of type " + factory.getClass(), e);
        }
    }

    @Bean
    public SAXTransformerFactory saxTransformerFactory() throws NrgServiceException {
        final SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            setAttributes(factory, XMLConstants.ACCESS_EXTERNAL_DTD, XMLConstants.ACCESS_EXTERNAL_STYLESHEET);
            return factory;
        } catch (TransformerConfigurationException e) {
            throw new NrgServiceException("Failed to set 'Secure Processing' feature on TransformerFactory implementation of type " + factory.getClass(), e);
        }
    }

    @Bean
    public SerializerService serializerService() throws SAXNotSupportedException, SAXNotRecognizedException, ParserConfigurationException, NrgServiceException {
        return new SerializerService(objectMapperBuilder(), documentBuilderFactory(), saxParserFactory(), transformerFactory(), saxTransformerFactory());
    }

    @Bean
    public ObjectMapper objectMapper(final SerializerService serializer) {
        return serializer.getObjectMapper();
    }

    private void setAttributes(final TransformerFactory factory, final String... attributes) {
        for (final String attribute : attributes) {
            try {
                factory.setAttribute(attribute, "");
            } catch (IllegalArgumentException e) {
                final Class<? extends TransformerFactory> factoryClass = factory.getClass();
                log.warn("Got an illegal argument exception setting attribute \"{}\" on a transformer factory instance of class {}. The library supplying this class should be deprecated and upgraded: {}", attribute, factoryClass.getName(), factoryClass.getProtectionDomain().getCodeSource().getLocation(), e);
            }
        }
    }

    private final List<Module> _jacksonModules = new ArrayList<>();
}
