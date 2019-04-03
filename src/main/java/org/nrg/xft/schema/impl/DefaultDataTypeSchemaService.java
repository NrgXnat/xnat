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
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class DefaultDataTypeSchemaService implements DataTypeSchemaService {
    @Autowired
    public DefaultDataTypeSchemaService(final DocumentBuilder builder) {
        _builder = builder;

        try {
            for (final Resource resource : BasicXnatResourceLocator.getResources("classpath*:schemas/*/*.xsd")) {
                try {
                    final String      schemaPath = StringUtils.substringAfterLast(resource.getURI().toString(), "/schemas/");
                    final String      schemaName = StringUtils.split(schemaPath, "/")[1];
                    final InputStream input      = new ReusableInputStream(resource.getInputStream());
                    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                        final Document document = parseSchema(reader);
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

    public static String getSchemaPath(final String namespace, final String schema) {
        return StringUtils.appendIfMissing(namespace + "/" + schema, ".xsd");
    }

    @Override
    public Document getSchema(final String namespace, final String schema) {
        final String schemaPath = getSchemaPath(namespace, schema);
        return _schemaDocs.containsKey(schemaPath) ? _schemaDocs.get(schemaPath) : _schemaDocs.get(schema);
    }

    @Override
    public Document getSchema(final String schemaPath) {
        return _schemaDocs.get(schemaPath);
    }

    @Override
    public String getSchemaContents(final String namespace, final String schema) {
        final String schemaPath = getSchemaPath(namespace, schema);
        return _schemas.containsKey(schemaPath) ? _schemas.get(schemaPath) : _schemas.get(schema);
    }

    @Override
    public String getSchemaContents(final String schemaPath) {
        return _schemas.get(schemaPath);
    }

    private Document parseSchema(final Reader schema) throws IOException, SAXException {
        return _builder.parse(new InputSource(schema));
    }

    private final DocumentBuilder _builder;

    private final Map<String, String>   _schemas    = new HashMap<>();
    private final Map<String, Document> _schemaDocs = new HashMap<>();
}
