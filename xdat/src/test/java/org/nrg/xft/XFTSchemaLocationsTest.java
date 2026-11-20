package org.nrg.xft;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nrg.framework.services.ContextService;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.references.XFTPseudonymManager;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.XFTManager;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Plugin schemas are read from jars and import core schemas by a relative path that only exists in the source tree,
 * so validation depends on the locations {@link XFT#GetAllSchemaLocations(String)} supplies for the core namespaces.
 */
public class XFTSchemaLocationsTest {
    private static final String SECURITY_NAMESPACE = "http://nrg.wustl.edu/security";
    private static final String PLUGIN_NAMESPACE   = "urn:test:plugin";

    // Imports the security schema by a relative path that does not exist inside the jar, as plugin schemas do.
    private static final String PLUGIN_SCHEMA = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\""
                                                + " xmlns:sec=\"" + SECURITY_NAMESPACE + "\""
                                                + " targetNamespace=\"" + PLUGIN_NAMESPACE + "\" elementFormDefault=\"qualified\">"
                                                + "<xs:import namespace=\"" + SECURITY_NAMESPACE + "\" schemaLocation=\"../security/security.xsd\"/>"
                                                + "<xs:element name=\"Root\"><xs:complexType><xs:sequence>"
                                                + "<xs:element name=\"role\" type=\"sec:role_type\" minOccurs=\"0\"/>"
                                                + "</xs:sequence></xs:complexType></xs:element>"
                                                + "</xs:schema>";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void loadSchema() throws Exception {
        // XFT looks up optional Spring beans through the static ContextService; a context left registered by an
        // earlier test in the same JVM would make missing beans throw instead of being skipped.
        ContextService.getInstance().setApplicationContext(null);
        final File schema = new File(XFTSchemaLocationsTest.class.getClassLoader().getResource("schemas/security/security.xsd").toURI());
        XFT.init(schema.getParentFile().getParentFile().getParent());
    }

    @AfterClass
    public static void unloadSchema() {
        XFTManager.clean();
        XFTMetaManager.clean();
        XFTReferenceManager.clean();
        XFTPseudonymManager.clean();
        GenericWrapperElement.ClearElementCache();
        ViewManager.FIELD_MAPS.clear();
        ViewManager.FIELD_NAMES.clear();
    }

    @Test
    public void suppliesFileSchemasAsUrls() {
        final String[] pairs = XFT.GetAllSchemaLocations(null).trim().split("\\s+");
        assertEquals("namespace/location pairs", 0, pairs.length % 2);
        boolean foundSecurity = false;
        for (int i = 0; i < pairs.length; i += 2) {
            assertTrue("location for " + pairs[i] + " should be a URL: " + pairs[i + 1], pairs[i + 1].startsWith("file:"));
            foundSecurity |= SECURITY_NAMESPACE.equals(pairs[i]);
        }
        assertTrue("security schema should be listed", foundSecurity);
    }

    @Test
    public void jarSchemaResolvesImportOfFileSchema() throws Exception {
        final File jar = folder.newFile("plugin.jar");
        try (final JarOutputStream out = new JarOutputStream(new FileOutputStream(jar))) {
            out.putNextEntry(new ZipEntry("schemas/plugin/plugin.xsd"));
            out.write(PLUGIN_SCHEMA.getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
        }
        final String locations = XFT.GetAllSchemaLocations(null) + " " + PLUGIN_NAMESPACE + " jar:" + jar.toURI() + "!/schemas/plugin/plugin.xsd";

        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        factory.setFeature("http://apache.org/xml/features/validation/schema", true);
        final SAXParser parser = factory.newSAXParser();
        parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        parser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", locations);

        final List<String> errors = new ArrayList<>();
        parser.parse(new InputSource(new StringReader("<p:Root xmlns:p=\"" + PLUGIN_NAMESPACE + "\"/>")), new DefaultHandler() {
            @Override
            public void error(final SAXParseException e) {
                errors.add(e.getMessage());
            }

            @Override
            public void fatalError(final SAXParseException e) {
                errors.add(e.getMessage());
            }
        });
        assertTrue("validation errors: " + errors, errors.isEmpty());
    }
}
