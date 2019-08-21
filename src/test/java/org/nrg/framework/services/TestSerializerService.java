/*
 * framework: org.nrg.framework.services.TestSerializerService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.services;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.assertj.core.util.Files;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestSerializerServiceConfiguration.class)
public class TestSerializerService {
    public TestSerializerService() {
        _javaVersion = Integer.parseInt(StringUtils.substringBefore(StringUtils.removeStart(System.getProperty("java.version"), "1."), "."));
    }

    @Autowired
    public void setSerializerService(final SerializerService serializer) {
        _serializer = serializer;
    }

    @Test
    public void testNaNAndInfinityHandling() throws IOException {
        final Map<String, Double> values = new HashMap<>();
        values.put("one", 3.14159);
        values.put("two", 723.12479145115);
        values.put("three", Double.MAX_VALUE);
        values.put("four", Double.MIN_VALUE);
        values.put("five", Double.MIN_NORMAL);
        values.put("six", Double.NaN);
        values.put("seven", Double.NEGATIVE_INFINITY);
        values.put("eight", Double.POSITIVE_INFINITY);

        final String serialized = _serializer.toJson(values);

        assertNotNull(serialized);

        final Map<String, Double> deserialized = _serializer.deserializeJson(serialized, SerializerService.TYPE_REF_MAP_STRING_DOUBLE);

        assertNotNull(deserialized);
        assertFalse(Double.isInfinite(deserialized.get("one")));
        assertFalse(Double.isInfinite(deserialized.get("two")));
        assertFalse(Double.isInfinite(deserialized.get("three")));
        assertFalse(Double.isInfinite(deserialized.get("four")));
        assertFalse(Double.isInfinite(deserialized.get("five")));
        assertFalse(Double.isNaN(deserialized.get("one")));
        assertFalse(Double.isNaN(deserialized.get("two")));
        assertFalse(Double.isNaN(deserialized.get("three")));
        assertFalse(Double.isNaN(deserialized.get("four")));
        assertFalse(Double.isNaN(deserialized.get("five")));
        assertTrue(Double.isNaN(deserialized.get("six")));
        assertTrue(Double.isInfinite(deserialized.get("seven")));
        assertTrue(Double.isInfinite(deserialized.get("eight")));
    }

    @Test
    public void testAnnotatedMixIn() throws IOException {
        final SimpleBean bean = new SimpleBean(RELEVANT, IGNORED);
        assertNotNull(bean);
        assertEquals(RELEVANT, bean.getRelevantField());
        assertEquals(IGNORED, bean.getIgnoredField());

        final String json = _serializer.toJson(bean);
        assertNotNull(json);
        final Map<String, String> map = _serializer.deserializeJsonToMapOfStrings(json);
        assertTrue(map.containsKey("relevantField"));
        assertFalse(map.containsKey("ignoredField"));
        assertEquals(RELEVANT, map.get("relevantField"));
    }

    @Test
    public void testXmlTransform() throws IOException, SAXException, TransformerException {
        final Document document = _serializer.parse(BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/services/no_xxe.xml"));
        final String   xml      = _serializer.toXml(document);

        assertTrue(StringUtils.isNotBlank(xml));

        final Writer writer = new StringWriter();
        _serializer.toXml(document, writer);
        final String written = writer.toString();

        assertTrue(StringUtils.isNotBlank(written));
        assertEquals(xml, written);

        final File temporaryFile = Files.newTemporaryFile();
        temporaryFile.deleteOnExit();

        _serializer.toXml(document, new FileWriter(temporaryFile));
        final String fromFile = FileUtils.readFileToString(temporaryFile, Charset.defaultCharset());

        assertTrue(StringUtils.isNotBlank(fromFile));
        assertEquals(xml, fromFile);
    }

    @Test(expected = SAXParseException.class)
    public void testEntityExpansionParseStringSuppression() throws IOException, SAXException, TransformerException {
        final Document plainParsed      = _serializer.parse(BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/services/no_xxe.xml"));
        final String   plainTransformed = _serializer.toXml(plainParsed);
        assertFalse(plainTransformed.contains("This is the stuff I injected."));

        final String   resolved = getResolvedXmlWithInjection();
        final Document document = _serializer.parse(resolved);
        checkJava7Suppression(document);
        fail("This test should have thrown a SAXParserException when parsing vulnerable XML.");
    }

    @Test(expected = SAXParseException.class)
    public void testEntityExpansionParseResourceSuppression() throws IOException, SAXException {
        final Resource resolved = getResolvedXmlWithInjectionAsResource();
        final Document document = _serializer.parse(resolved);
        checkJava7Suppression(document);
        fail("This test should have thrown a SAXParserException when parsing vulnerable XML.");
    }

    @Test(expected = SAXParseException.class)
    public void testEntityExpansionParseInputStreamSuppression() throws IOException, SAXException {
        final InputStream resolved = getResolvedXmlWithInjectionAsInputStream();
        final Document    document = _serializer.parse(resolved);
        checkJava7Suppression(document);
        fail("This test should have thrown a SAXParserException when parsing vulnerable XML.");
    }

    @Test(expected = SAXParseException.class)
    public void testEntityExpansionSaxParseInputStreamSuppression() throws IOException, SAXException, ParserConfigurationException {
        final Resource resource = BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/services/no_xxe.xml");
        final String   xml      = IOUtils.toString(new FileReader(resource.getFile()));

        final TestValidationHandler handler1 = new TestValidationHandler();
        _serializer.parseText(xml, handler1);
        assertTrue(handler1.isValid());

        final TestValidationHandler handler2 = new TestValidationHandler();
        _serializer.parse(resource.getURL().toString(), handler2);
        assertTrue(handler2.isValid());

        final Resource              resolved = getResolvedXmlWithInjectionAsResource();
        final TestValidationHandler handler3 = new TestValidationHandler("hack", "This is the stuff I injected.");
        _serializer.parseText(IOUtils.toString(new FileReader(resolved.getFile())), handler3);
        checkJava7Suppression(handler3);
    }

    @Test(expected = SAXParseException.class)
    public void testBillionLaughsSuppression() throws IOException, SAXException {
        final Resource billionLaughsXml = BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/services/billion_laughs.xml");
        assertNotNull(billionLaughsXml);
        assertTrue(billionLaughsXml.contentLength() > 0);
        _serializer.parse(billionLaughsXml);
        fail("This test should have thrown a SAXParserException when parsing vulnerable XML.");
    }

    @Test(expected = SAXParseException.class)
    public void testBillionLaughsValidateSchemaSuppression() throws Exception {
        final Resource       billionLaughsXml = BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/services/billion_laughs.xml");
        final DefaultHandler handler          = _serializer.validateSchema(billionLaughsXml.getURL().toString(), null);
        assertNotNull(handler);
        fail("This test should have thrown a SAXParserException when parsing vulnerable XML.");
    }

    @Test(expected = SAXParseException.class)
    public void testBillionLaughsValidateResourceSuppression() throws Exception {
        final Resource       billionLaughsXml = BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/services/billion_laughs.xml");
        final DefaultHandler handler          = _serializer.validateResource(billionLaughsXml, null);
        assertNotNull(handler);
        fail("This test should have thrown a SAXParserException when parsing vulnerable XML.");
    }

    @Test(expected = SAXParseException.class)
    public void testBillionLaughsValidateReaderSuppression() throws Exception {
        final Resource       billionLaughsXml = BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/services/billion_laughs.xml");
        final DefaultHandler handler          = _serializer.validateReader(new InputStreamReader(billionLaughsXml.getInputStream()), null);
        assertNotNull(handler);
        fail("This test should have thrown a SAXParserException when parsing vulnerable XML.");
    }

    @Test(expected = SAXParseException.class)
    public void testBillionLaughsValidateInputStreamSuppression() throws Exception {
        final Resource       billionLaughsXml = BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/services/billion_laughs.xml");
        final DefaultHandler handler          = _serializer.validateInputStream(billionLaughsXml.getInputStream(), null);
        assertNotNull(handler);
        fail("This test should have thrown a SAXParserException when parsing vulnerable XML.");
    }

    @Test(expected = SAXParseException.class)
    public void testBillionLaughsValidateStringSuppression() throws Exception {
        final StringWriter billionLaughsXml = new StringWriter();
        IOUtils.copy(BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/services/billion_laughs.xml").getInputStream(), billionLaughsXml, Charset.defaultCharset());
        final DefaultHandler handler = _serializer.validateString(billionLaughsXml.toString(), null);
        assertNotNull(handler);
        fail("This test should have thrown a SAXParserException when parsing vulnerable XML.");
    }

    @Test(expected = SAXParseException.class)
    public void testBillionLaughsValidateSuppression() throws Exception {
        final Resource       billionLaughsXml = BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/services/billion_laughs.xml");
        final DefaultHandler handler          = _serializer.validate(new InputSource(billionLaughsXml.getInputStream()), null);
        assertNotNull(handler);
        fail("This test should have thrown a SAXParserException when parsing vulnerable XML.");
    }

    private String getResolvedXmlWithInjection() throws IOException {
        final Resource injected = BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/services/injected.txt");
        return StringSubstitutor.replace(BasicXnatResourceLocator.asString("classpath:org/nrg/framework/services/xxe_with_injection_point.xml"), ImmutableMap.<String, Object>of("injectedPath", injected.getURI().toString()));
    }

    private Resource getResolvedXmlWithInjectionAsResource() throws IOException {
        final File xmlFile = Files.newTemporaryFile();
        xmlFile.deleteOnExit();

        final String resolvedXmlWithInjection = getResolvedXmlWithInjection();
        try (final StringReader reader = new StringReader(resolvedXmlWithInjection);
             final FileWriter writer = new FileWriter(xmlFile)) {
            IOUtils.copy(reader, writer);
        }
        return new FileSystemResource(xmlFile);
    }

    private InputStream getResolvedXmlWithInjectionAsInputStream() throws IOException {
        return getResolvedXmlWithInjectionAsResource().getInputStream();
    }

    private void checkJava7Suppression(final Document document) throws SAXParseException {
        if (_javaVersion > 7) {
            return;
        }
        final NodeList descriptions = document.getDocumentElement().getElementsByTagName("xnat:description");
        if (descriptions.getLength() != 1) {
            throw new RuntimeException("Unexpected context: Java 7 but the XML document doesn't appear to have processed at all, as there's no \"xnat:description\" node.");
        }
        final String content = descriptions.item(0).getTextContent();
        if (StringUtils.contains(content, "This is the stuff I injected.")) {
            throw new RuntimeException("The injected entity value was expanded by the SAX parser, which is bad.");
        }
        throw new SAXParseException("This isn't a real SAXParserException, but indicates that the &hack entity wasn't expanded.", "id", "id", 14, 1);
    }

    private void checkJava7Suppression(final TestValidationHandler handler) throws SAXParseException {
        if (_javaVersion > 7) {
            return;
        }
        final List<String> unhandled = handler.getUnskippedEntities();
        if (!unhandled.isEmpty()) {
            throw new RuntimeException(unhandled.size() + " entities were expected but weren't skipped, which is bad: " + StringUtils.join(unhandled, ", "));
        }
        final String content = handler.getText();
        if (StringUtils.contains(content, "This is the stuff I injected.")) {
            throw new RuntimeException("The injected entity value was expanded by the SAX parser, which is bad.");
        }
        throw new SAXParseException("This isn't a real SAXParserException, but indicates that the expected entities weren't expanded: " + StringUtils.join(handler.getExpectedEntities().keySet(), ", "), "id", "id", 14, 1);
    }

    private static final String IGNORED  = "This shouldn't show up.";
    private static final String RELEVANT = "This should totally show up.";

    private final int _javaVersion;

    private SerializerService _serializer;
}
