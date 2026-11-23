package org.nrg.xft;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nrg.framework.configuration.SerializerConfig;
import org.nrg.framework.services.ContextService;
import org.nrg.framework.services.SerializerService;
import org.nrg.framework.services.impl.ValidationHandler;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.references.XFTPseudonymManager;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.XFTManager;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
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

        // Validate through SerializerService, as XMLValidator does, so the test runs with secure processing on and the
        // ACCESS_EXTERNAL_SCHEMA protocol list production uses, not a parser configured just for the test.
        final SerializerConfig  config  = new SerializerConfig();
        final SerializerService service = new SerializerService(config.objectMapperBuilder(), config.documentBuilderFactory(), config.saxParserFactory(), config.transformerFactory(), config.saxTransformerFactory());
        final ValidationHandler handler = service.validateString("<p:Root xmlns:p=\"" + PLUGIN_NAMESPACE + "\"/>", locations);
        assertTrue("validation errors: " + handler.getErrors(), handler.assertValid());
    }
}
