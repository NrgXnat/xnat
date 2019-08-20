package org.nrg.framework.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.filters.StringInputStream;
import org.nrg.framework.services.impl.ValidationHandler;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.framework.utilities.ImmutableProperties;
import org.nrg.framework.utilities.PropertiesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.nrg.framework.utilities.PropertiesUtils.of;

@Service
@Slf4j
@Getter
@Accessors(prefix = "_")
public class SerializerService {
    public static final TypeReference<ArrayList<String>>                  TYPE_REF_LIST_STRING            = new TypeReference<ArrayList<String>>() {
    };
    public static final TypeReference<HashMap<String, ArrayList<String>>> TYPE_REF_MAP_STRING_LIST_STRING = new TypeReference<HashMap<String, ArrayList<String>>>() {
    };
    public static final TypeReference<HashMap<String, Double>>            TYPE_REF_MAP_STRING_DOUBLE      = new TypeReference<HashMap<String, Double>>() {
    };
    public static final TypeReference<HashMap<String, String>>            TYPE_REF_MAP_STRING_STRING      = new TypeReference<HashMap<String, String>>() {
    };

    @Autowired
    public SerializerService(final Jackson2ObjectMapperBuilder builder, final DocumentBuilderFactory documentBuilderFactory, final SAXParserFactory saxParserFactory, final TransformerFactory transformerFactory, final SAXTransformerFactory saxTransformerFactory) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        _objectMapper = builder.build();
        _yamlObjectMapper = new YamlObjectMapper();
        _documentBuilderFactory = documentBuilderFactory;
        _saxParserFactory = saxParserFactory;
        _transformerFactory = transformerFactory;
        _saxTransformerFactory = saxTransformerFactory;

        _documentBuilder = _documentBuilderFactory.newDocumentBuilder();

        _validatingSaxParserFactory = SAXParserFactory.newInstance();
        _validatingSaxParserFactory.setNamespaceAware(true);
        _validatingSaxParserFactory.setValidating(true);
        _validatingSaxParserFactory.setFeature("http://apache.org/xml/features/validation/schema", true);
    }

    public JsonNode deserializeJson(final String json) throws IOException {
        return _objectMapper.readTree(json);
    }

    public <T> T deserializeJson(final String json, final Class<T> clazz) throws IOException {
        return _objectMapper.readValue(json, clazz);
    }

    public <T> T deserializeJson(final String json, final TypeReference<T> typeRef) throws IOException {
        return _objectMapper.readValue(json, typeRef);
    }

    public <T> T deserializeJson(final String json, final JavaType type) throws IOException {
        return _objectMapper.readValue(json, type);
    }

    public JsonNode deserializeJson(final InputStream input) throws IOException {
        return _objectMapper.readTree(input);
    }

    public Map<String, String> deserializeJsonToMapOfStrings(final String json) throws IOException {
        return _objectMapper.readValue(json, TYPE_REF_MAP_STRING_STRING);
    }

    public <T> String toJson(final T instance) throws IOException {
        return _objectMapper.writeValueAsString(instance);
    }

    public JsonNode deserializeYaml(final String yaml) throws IOException {
        return _yamlObjectMapper.readTree(yaml);
    }

    public <T> T deserializeYaml(final String yaml, Class<T> clazz) throws IOException {
        return _yamlObjectMapper.readValue(yaml, clazz);
    }

    public <T> T deserializeYaml(final String yaml, final TypeReference<T> typeRef) throws IOException {
        return _yamlObjectMapper.readValue(yaml, typeRef);
    }

    public <T> T deserializeYaml(final String json, final JavaType type) throws IOException {
        return _yamlObjectMapper.readValue(json, type);
    }

    public JsonNode deserializeYaml(final InputStream input) throws IOException {
        return _yamlObjectMapper.readTree(input);
    }

    public <T> T deserializeYaml(final InputStream input, final TypeReference<T> typeRef) throws IOException {
        return _yamlObjectMapper.readValue(input, typeRef);
    }

    public <T> String toYaml(final T instance) throws IOException {
        return _yamlObjectMapper.writeValueAsString(instance);
    }

    public String toXml(final Document document) throws TransformerException {
        final StringWriter writer = new StringWriter();
        toXml(document, writer, ImmutableProperties.emptyProperties());
        return writer.getBuffer().toString();
    }

    public void toXml(final Document document, final Writer writer) throws TransformerException {
        toXml(document, writer, ImmutableProperties.emptyProperties());
    }

    public String toXml(final Document document, final Properties properties) throws TransformerException {
        final StringWriter writer = new StringWriter();
        toXml(document, writer, properties);
        return writer.getBuffer().toString();
    }

    public void toXml(final Document document, final Writer writer, final Properties properties) throws TransformerException {
        final Transformer transformer = _transformerFactory.newTransformer();
        transformer.setOutputProperties(PropertiesUtils.combine(properties, DEFAULT_XML_TRANSFORM_PROPERTIES));
        transformer.transform(new DOMSource(document), new StreamResult(writer));
    }

    public DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        return _documentBuilderFactory.newDocumentBuilder();
    }

    public SAXParser getSAXParser() throws ParserConfigurationException, SAXException {
        return _saxParserFactory.newSAXParser();
    }

    public Transformer getTransformer() throws TransformerConfigurationException {
        return _transformerFactory.newTransformer();
    }

    public Transformer getSAXTransformer() throws TransformerConfigurationException {
        return _saxTransformerFactory.newTransformer();
    }

    public TransformerHandler getSAXTransformerHandler() throws TransformerConfigurationException {
        return _saxTransformerFactory.newTransformerHandler();
    }

    public Document parse(final Resource resource) throws IOException, SAXException {
        return parse(BasicXnatResourceLocator.asString(resource));
    }

    public Document parse(final String text) throws IOException, SAXException {
        return parse(new StringInputStream(text));
    }

    public Document parse(final InputStream input) throws IOException, SAXException {
        return _documentBuilder.parse(input);
    }

    public void parseText(final String xml, final DefaultHandler handler) throws ParserConfigurationException, SAXException, IOException {
        parseText(xml, handler, Collections.<String, Object>emptyMap());
    }

    public void parseText(final String xml, final DefaultHandler handler, final Object... properties) throws ParserConfigurationException, SAXException, IOException {
        parseText(xml, handler, of(properties));
    }

    public void parseText(final String xml, final DefaultHandler handler, final Map<String, Object> properties) throws ParserConfigurationException, SAXException, IOException {
        final SAXParser parser = getSAXParser();
        if (MapUtils.isNotEmpty(properties)) {
            for (final String key : properties.keySet()) {
                parser.setProperty(key, properties.get(key));
            }
        }
        parser.parse(new StringInputStream(xml), handler);
    }

    public void parse(final String documentUrl, final DefaultHandler handler) throws ParserConfigurationException, SAXException, IOException {
        parse(documentUrl, handler, Collections.<String, Object>emptyMap());
    }

    public void parse(final Resource resource, final DefaultHandler handler) throws ParserConfigurationException, SAXException, IOException {
        parse(resource.getInputStream(), handler, Collections.<String, Object>emptyMap());
    }

    public void parse(final Reader reader, final DefaultHandler handler) throws ParserConfigurationException, SAXException, IOException {
        parse(new InputSource(reader), handler, Collections.<String, Object>emptyMap());
    }

    public void parse(final InputStream inputStream, final DefaultHandler handler) throws ParserConfigurationException, SAXException, IOException {
        parse(inputStream, handler, Collections.<String, Object>emptyMap());
    }

    public void parse(final InputSource inputSource, final DefaultHandler handler) throws ParserConfigurationException, SAXException, IOException {
        parse(inputSource, handler, Collections.<String, Object>emptyMap());
    }

    public void parse(final String documentUrl, final DefaultHandler handler, final Object... properties) throws ParserConfigurationException, SAXException, IOException {
        parse(documentUrl, handler, of(properties));
    }

    public void parse(final Resource resource, final DefaultHandler handler, final Object... properties) throws ParserConfigurationException, SAXException, IOException {
        parse(resource.getInputStream(), handler, of(properties));
    }

    public void parse(final Reader reader, final DefaultHandler handler, final Object... properties) throws ParserConfigurationException, SAXException, IOException {
        parse(new InputSource(reader), handler, of(properties));
    }

    public void parse(final InputStream inputStream, final DefaultHandler handler, final Object... properties) throws ParserConfigurationException, SAXException, IOException {
        parse(inputStream, handler, of(properties));
    }

    public void parse(final InputSource inputSource, final DefaultHandler handler, final Object... properties) throws ParserConfigurationException, SAXException, IOException {
        parse(inputSource, handler, of(properties));
    }

    public void parse(final String documentUrl, final DefaultHandler handler, final Map<String, Object> properties) throws ParserConfigurationException, SAXException, IOException {
        final SAXParser parser = getSAXParser();
        if (MapUtils.isNotEmpty(properties)) {
            for (final String key : properties.keySet()) {
                parser.setProperty(key, properties.get(key));
            }
        }
        parser.parse(documentUrl, handler);
    }

    public void parse(final Resource resource, final DefaultHandler handler, final Map<String, Object> properties) throws ParserConfigurationException, SAXException, IOException {
        final SAXParser parser = getSAXParser();
        if (MapUtils.isNotEmpty(properties)) {
            for (final String key : properties.keySet()) {
                parser.setProperty(key, properties.get(key));
            }
        }
        parser.parse(resource.getInputStream(), handler);
    }

    public void parse(final Reader reader, final DefaultHandler handler, final Map<String, Object> properties) throws ParserConfigurationException, SAXException, IOException {
        final SAXParser parser = getSAXParser();
        if (MapUtils.isNotEmpty(properties)) {
            for (final String key : properties.keySet()) {
                parser.setProperty(key, properties.get(key));
            }
        }
        parser.parse(new InputSource(reader), handler);
    }

    public void parse(final InputStream inputStream, final DefaultHandler handler, final Map<String, Object> properties) throws ParserConfigurationException, SAXException, IOException {
        final SAXParser parser = getSAXParser();
        if (MapUtils.isNotEmpty(properties)) {
            for (final String key : properties.keySet()) {
                parser.setProperty(key, properties.get(key));
            }
        }
        parser.parse(inputStream, handler);
    }

    public void parse(final InputSource inputSource, final DefaultHandler handler, final Map<String, Object> properties) throws ParserConfigurationException, SAXException, IOException {
        final SAXParser parser = getSAXParser();
        if (MapUtils.isNotEmpty(properties)) {
            for (final String key : properties.keySet()) {
                parser.setProperty(key, properties.get(key));
            }
        }
        parser.parse(inputSource, handler);
    }

    public ValidationHandler validateSchema(final String documentUrl, final String schemaLocations) throws Exception {
        return validate(new InputSource(documentUrl), schemaLocations);
    }

    public ValidationHandler validateResource(final Resource resource, final String schemaLocations) throws Exception {
        return validate(new InputSource(resource.getInputStream()), schemaLocations);
    }

    public ValidationHandler validateReader(final Reader reader, final String schemaLocations) throws Exception {
        return validate(new InputSource(reader), schemaLocations);
    }

    public ValidationHandler validateInputStream(final InputStream inputStream, final String schemaLocations) throws Exception {
        return validate(new InputSource(inputStream), schemaLocations);
    }

    public ValidationHandler validateString(final String xml, final String schemaLocations) throws Exception {
        return validate(new InputSource(new StringReader(xml)), schemaLocations);
    }

    public ValidationHandler validate(final InputSource inputSource, final String schemaLocations) throws Exception {
        final SAXParser parser = _validatingSaxParserFactory.newSAXParser();
        if (StringUtils.isNotBlank(schemaLocations)) {
            parser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", schemaLocations);
        }
        final ValidationHandler validator = new ValidationHandler();
        parser.parse(inputSource, validator);
        return validator;
    }

    public TypeFactory getTypeFactory() {
        return _objectMapper.getTypeFactory();
    }

    private static final Properties DEFAULT_XML_TRANSFORM_PROPERTIES = ImmutableProperties.builder().property(OutputKeys.INDENT, "yes").property("{http://xml.apache.org/xslt}indent-amount", "4").build();

    private final ObjectMapper           _objectMapper;
    private final YamlObjectMapper       _yamlObjectMapper;
    private final DocumentBuilderFactory _documentBuilderFactory;
    private final SAXParserFactory       _saxParserFactory;
    private final SAXParserFactory       _validatingSaxParserFactory;
    private final TransformerFactory     _transformerFactory;
    private final SAXTransformerFactory  _saxTransformerFactory;
    private final DocumentBuilder        _documentBuilder;
}
