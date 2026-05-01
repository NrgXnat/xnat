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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.entry;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
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
        assertThat(serialized).isNotNull().isNotEmpty();

        final Map<String, Double> deserialized = _serializer.deserializeJson(serialized, SerializerService.TYPE_REF_MAP_STRING_DOUBLE);
        assertThat(deserialized).isNotNull()
                                .isNotEmpty();
        assertThat(Double.isInfinite(deserialized.get("one"))).isFalse();
        assertThat(Double.isInfinite(deserialized.get("two"))).isFalse();
        assertThat(Double.isInfinite(deserialized.get("three"))).isFalse();
        assertThat(Double.isInfinite(deserialized.get("four"))).isFalse();
        assertThat(Double.isInfinite(deserialized.get("five"))).isFalse();
        assertThat(Double.isNaN(deserialized.get("one"))).isFalse();
        assertThat(Double.isNaN(deserialized.get("two"))).isFalse();
        assertThat(Double.isNaN(deserialized.get("three"))).isFalse();
        assertThat(Double.isNaN(deserialized.get("four"))).isFalse();
        assertThat(Double.isNaN(deserialized.get("five"))).isFalse();
        assertThat(Double.isNaN(deserialized.get("six"))).isTrue();
        assertThat(Double.isInfinite(deserialized.get("seven"))).isTrue();
        assertThat(Double.isInfinite(deserialized.get("eight"))).isTrue();
    }

    @Test
    public void testAnnotatedMixIn() throws IOException {
        final SimpleBean bean = new SimpleBean(RELEVANT, IGNORED);
        assertThat(bean).isNotNull()
                        .hasFieldOrPropertyWithValue("relevantField", RELEVANT)
                        .hasFieldOrPropertyWithValue("ignoredField", IGNORED);

        final String json = _serializer.toJson(bean);
        assertThat(json).isNotNull().isNotEmpty();
        final Map<String, String> map = _serializer.deserializeJsonToMapOfStrings(json);
        assertThat(map).isNotNull()
                       .isNotEmpty()
                       .contains(entry("relevantField", RELEVANT))
                       .doesNotContainKey("ignoredField");
    }

    @Test
    public void testXmlTransform() throws IOException, SAXException, TransformerException {
        final Document document = _serializer.parse(BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/services/no_xxe.xml"));
        final String   xml      = _serializer.toXml(document);
        assertThat(xml).isNotNull().isNotEmpty();

        final Writer writer = new StringWriter();
        _serializer.toXml(document, writer);
        final String written = writer.toString();

        assertThat(written).isNotNull().isNotEmpty().isEqualTo(xml);

        final File temporaryFile = Files.newTemporaryFile();
        temporaryFile.deleteOnExit();

        _serializer.toXml(document, new FileWriter(temporaryFile));
        final String fromFile = FileUtils.readFileToString(temporaryFile, Charset.defaultCharset());
        assertThat(fromFile).isNotNull().isNotEmpty().isEqualTo(xml);
    }

    @Test
    public void testEntityExpansionParseStringSuppression() {
        assertThrows(SAXParseException.class, () -> {
            final Document plainParsed      = _serializer.parse(BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/services/no_xxe.xml"));
            final String   plainTransformed = _serializer.toXml(plainParsed);
            assertThat(plainTransformed).isNotNull()
                                        .doesNotContain("This is the stuff I injected.");

            final String   resolved = getResolvedXmlWithInjection();
            final Document document = _serializer.parse(resolved);
            checkJava7Suppression(document);
        });
    }

    @Test
    public void testEntityExpansionParseResourceSuppression() {
        assertThrows(SAXParseException.class, () -> {
            final Resource resolved = getResolvedXmlWithInjectionAsResource();
            final Document document = _serializer.parse(resolved);
            checkJava7Suppression(document);
        });
    }

    @Test
    public void testEntityExpansionParseInputStreamSuppression() {
        assertThrows(SAXParseException.class, () -> {
            final InputStream resolved = getResolvedXmlWithInjectionAsInputStream();
            final Document    document = _serializer.parse(resolved);
            checkJava7Suppression(document);
        });
    }

    @Test
    public void testEntityExpansionSaxParseInputStreamSuppression() {
        assertThrows(SAXParseException.class, () -> {
            final Resource resource = BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/services/no_xxe.xml");
            final String   xml      = IOUtils.toString(new FileReader(resource.getFile()));

            final TestValidationHandler handler1 = new TestValidationHandler();
            _serializer.parseText(xml, handler1);
            assertThat(handler1).isNotNull()
                                .hasFieldOrPropertyWithValue("valid", true);

            final TestValidationHandler handler2 = new TestValidationHandler();
            _serializer.parse(resource.getURL().toString(), handler2);
            assertThat(handler2).isNotNull()
                                .hasFieldOrPropertyWithValue("valid", true);

            final Resource              resolved = getResolvedXmlWithInjectionAsResource();
            final TestValidationHandler handler3 = new TestValidationHandler("hack", "This is the stuff I injected.");
            _serializer.parseText(IOUtils.toString(new FileReader(resolved.getFile())), handler3);
            checkJava7Suppression(handler3);
        });
    }

    @Test
    public void testBillionLaughsSuppression() {
        assertThrows(SAXParseException.class, () -> {
            final Resource billionLaughsXml = BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/services/billion_laughs.xml");
            assertThat(billionLaughsXml).isNotNull();
            assertThat(billionLaughsXml.contentLength()).isNotNull().isGreaterThan(0);
            _serializer.parse(billionLaughsXml);
        });
    }

    @Test
    public void testBillionLaughsValidateSchemaSuppression() {
        assertThrows(SAXParseException.class, () -> {
            final Resource       billionLaughsXml = BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/services/billion_laughs.xml");
            final DefaultHandler handler          = _serializer.validateSchema(billionLaughsXml.getURL().toString(), null);
            assertThat(handler).isNotNull();
        });
    }

    @Test
    public void testBillionLaughsValidateResourceSuppression() {
        assertThrows(SAXParseException.class, () -> {
            final Resource       billionLaughsXml = BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/services/billion_laughs.xml");
            final DefaultHandler handler          = _serializer.validateResource(billionLaughsXml, null);
            assertThat(handler).isNotNull();
        });
    }

    @Test
    public void testBillionLaughsValidateReaderSuppression() {
        assertThrows(SAXParseException.class, () -> {
            final Resource       billionLaughsXml = BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/services/billion_laughs.xml");
            final DefaultHandler handler          = _serializer.validateReader(new InputStreamReader(billionLaughsXml.getInputStream()), null);
            assertThat(handler).isNotNull();
        });
    }

    @Test
    public void testBillionLaughsValidateInputStreamSuppression() {
        assertThrows(SAXParseException.class, () -> {
            final Resource       billionLaughsXml = BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/services/billion_laughs.xml");
            final DefaultHandler handler          = _serializer.validateInputStream(billionLaughsXml.getInputStream(), null);
            assertThat(handler).isNotNull();
        });
    }

    @Test
    public void testBillionLaughsValidateStringSuppression() {
        assertThrows(SAXParseException.class, () -> {
            final StringWriter billionLaughsXml = new StringWriter();
            IOUtils.copy(BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/services/billion_laughs.xml").getInputStream(), billionLaughsXml, Charset.defaultCharset());
            final DefaultHandler handler = _serializer.validateString(billionLaughsXml.toString(), null);
            assertThat(handler).isNotNull();
        });
    }

    @Test
    public void testBillionLaughsValidateSuppression() {
        assertThrows(SAXParseException.class, () -> {
            final Resource       billionLaughsXml = BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/services/billion_laughs.xml");
            final DefaultHandler handler          = _serializer.validate(new InputSource(billionLaughsXml.getInputStream()), null);
            assertThat(handler).isNotNull();
        });
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
