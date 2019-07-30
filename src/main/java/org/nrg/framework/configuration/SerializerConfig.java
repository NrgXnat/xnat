package org.nrg.framework.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
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
import java.util.Map;

@Configuration
@Slf4j
public class SerializerConfig {
    @Autowired
    public void setJacksonModules(final Module[] jacksonModules) {
        log.info("Adding {} Jackson modules", jacksonModules != null ? jacksonModules.length : 0);
        _jacksonModules = jacksonModules;
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
                .featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS, SerializationFeature.WRITE_NULL_MAP_VALUES)
                .modulesToInstall(_jacksonModules);
    }

    @Bean
    public DocumentBuilderFactory documentBuilderFactory() throws NrgServiceException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setExpandEntityReferences(false);
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
            return factory;
        } catch (TransformerConfigurationException e) {
            throw new NrgServiceException("Failed to set 'Secure Processing' feature on TransformerFactory implementation of type " + factory.getClass(), e);
        }
    }

    @Bean
    public SerializerService serializerService() throws SAXNotSupportedException, SAXNotRecognizedException, ParserConfigurationException, NrgServiceException {
        return new SerializerService(objectMapperBuilder(), documentBuilderFactory(), saxParserFactory(), transformerFactory(), saxTransformerFactory());
    }

    private Module[] _jacksonModules;
}
