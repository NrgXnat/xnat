package org.nrg.xft.schema.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.framework.utilities.ReusableInputStream;
import org.nrg.xft.schema.DataTypeSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class DefaultDataTypeSchemaService implements DataTypeSchemaService {
    @Autowired
    public DefaultDataTypeSchemaService(final DocumentBuilder builder) {
        try {
            for (final Resource resource : BasicXnatResourceLocator.getResources("classpath*:schemas/*/*.xsd")) {
                try {
                    final String      schemaPath = StringUtils.substringAfterLast(resource.getURI().toString(), "/schemas/");
                    final String      schemaName = StringUtils.split(schemaPath, "/")[1];
                    final InputStream input      = new ReusableInputStream(resource.getInputStream());
                    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                        final Document document = builder.parse(new InputSource(reader));
                        _schemaDocs.put(schemaPath, document);
                        _schemaDocs.put(schemaName, document);
                    }
                    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                        final String contents = IOUtils.toString(reader);
                        _schemas.put(schemaPath, contents);
                        _schemas.put(schemaName, contents);
                    }
                } catch (IOException e) {
                    log.error("An error occurred trying to read the contents of the resource {}", resource, e);
                } catch (SAXException e) {
                    log.error("An error occurred trying to parse the contents of the resource {}", resource, e);
                }
            }
        } catch (IOException e) {
            log.error("An error occurred trying to read resources matching the pattern \"classpath*:schemas/*/*.xsd\"", e);
        }
    }

    /**
     * Gets the path for the submitted schema. For this version of this method, the path is the schema name
     * as containing folder with any extensions stripped, then the schema name as the file name, with the
     * extension ".xsd" added if not present. For example, if the requested schema name is <b>foo</b>, the
     * resulting schema path would be <b>foo/foo.xsd</b>.
     *
     * @param schema The schema to get a path for.
     *
     * @return The calculated schema path.
     */
    public static String getSchemaPath(final String schema) {
        return getSchemaPath(StringUtils.removeEnd(schema, ".xsd"), schema);
    }

    /**
     * Gets the path for the submitted schema. For this version of this method, the path is the namespace
     * as containing folder and the schema name as the file name, with the extension ".xsd" added if not
     * present. For example, if the requested namespace is <b>foo</b> and requested schema name is
     * <b>bar</b>, the resulting schema path would be <b>foo/bar.xsd</b>.
     *
     * @param namespace The namespace to get a path for.
     * @param schema    The schema to get a path for.
     *
     * @return The calculated schema path.
     */
    public static String getSchemaPath(final String namespace, final String schema) {
        return StringUtils.appendIfMissing(namespace + "/" + schema, ".xsd");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document getSchema(final String namespace, final String schema) {
        final String schemaPath = getSchemaPath(namespace, schema);
        return _schemaDocs.containsKey(schemaPath) ? _schemaDocs.get(schemaPath) : _schemaDocs.get(schema);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document getSchema(final String schema) {
        return _schemaDocs.get(schema);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSchemaContents(final String namespace, final String schema) {
        final String schemaPath = getSchemaPath(namespace, schema);
        return _schemas.containsKey(schemaPath) ? _schemas.get(schemaPath) : _schemas.get(schema);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSchemaContents(final String schema) {
        return _schemas.containsKey(schema) ? _schemas.get(schema) : _schemas.get(getSchemaPath(schema));
    }

    private final Map<String, String>   _schemas    = new HashMap<>();
    private final Map<String, Document> _schemaDocs = new HashMap<>();
}
