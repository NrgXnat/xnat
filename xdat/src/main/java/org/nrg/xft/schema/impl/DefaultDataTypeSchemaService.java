package org.nrg.xft.schema.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xft.schema.DataTypeSchemaService;
import org.nrg.xft.schema.db.entities.DBBackedSchema;
import org.nrg.xft.schema.db.services.DBBackedSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class DefaultDataTypeSchemaService implements DataTypeSchemaService {
    @Autowired
    public DefaultDataTypeSchemaService(final DocumentBuilder builder) {
        this.documentBuilder = builder;

        List<Resource> schemaResources;
        try {
            schemaResources = BasicXnatResourceLocator.getResources("classpath*:schemas/*/*.xsd");
        } catch (IOException e) {
            log.error("An error occurred trying to read resources matching the pattern \"classpath*:schemas/*/*.xsd\"", e);
            schemaResources = Collections.emptyList();
        }

        // Build map to look up schema resource by schema name or schema path
        schemas = schemaResources.stream()
                .map(resource -> {
                    String resourceUri;
                    try {
                        resourceUri = resource.getURI().toString();
                    } catch (IOException e) {
                        log.error("An error occurred trying to read the contents of the resource {}", resource, e);
                        resourceUri = null;
                    }

                    return resourceUri == null ? null : Pair.of(resourceUri, resource);
                })
                .filter(Objects::nonNull)
                .flatMap(pair -> {
                    final String resourceUri = pair.getLeft();
                    final Resource resource = pair.getRight();

                    final String schemaPath = StringUtils.substringAfterLast(resourceUri, "/schemas/");
                    final String schemaName = StringUtils.split(schemaPath, "/")[1];
                    return Stream.of(
                        Pair.of(schemaPath, resource),
                        Pair.of(schemaName, resource)
                    );
                })
                .collect(Collectors.collectingAndThen(Collectors.toMap(Pair::getLeft, Pair::getRight, (p, q) -> p), Collections::unmodifiableMap));
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

    @Autowired(required = false)
    public void setDbBackedSchemaService(final DBBackedSchemaService dbBackedSchemaService) {
        _dbBackedSchemaService = dbBackedSchemaService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document getSchema(final String namespace, final String schema) {
        final Resource resource = getResource(namespace, schema);
        if (resource != null) {
            final Document doc = getSchemaDoc(resource);
            if (doc != null) {
                return doc;
            }
        }
        return getSchemaDocFromDB(namespace, schema);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document getSchema(final String schema) {
        final Resource resource = getResource(schema);
        if (resource != null) {
            final Document doc = getSchemaDoc(resource);
            if (doc != null) {
                return doc;
            }
        }
        final String baseName = StringUtils.removeEnd(schema, ".xsd");
        return getSchemaDocFromDB(baseName, schema);
    }

    private Document getSchemaDoc(final Resource resource) {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return documentBuilder.parse(new InputSource(reader));
        } catch (IOException | SAXException e) {
            log.error("An error occurred trying to read resource {}", resource, e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSchemaContents(final String namespace, final String schema) {
        log.debug("getSchemaContents called with namespace={}, schema={}", namespace, schema);
        final Resource resource = getResource(namespace, schema);
        if (resource != null) {
            log.debug("Found classpath resource for namespace={}, schema={}: {}", namespace, schema, resource);
            final String contents = getSchemaContents(resource);
            if (contents != null) {
                return contents;
            }
        }
        log.debug("No classpath resource for namespace={}, schema={}, trying DB fallback", namespace, schema);
        return getSchemaContentsFromDB(namespace, schema);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSchemaContents(final String schema) {
        log.debug("getSchemaContents called with schema={}", schema);
        final Resource resource = getResource(schema);
        if (resource != null) {
            log.debug("Found classpath resource for schema={}: {}", schema, resource);
            final String contents = getSchemaContents(resource);
            if (contents != null) {
                return contents;
            }
        }
        log.debug("No classpath resource for schema={}, trying DB fallback", schema);
        final String baseName = StringUtils.removeEnd(schema, ".xsd");
        return getSchemaContentsFromDB(baseName, schema);
    }

    private String getSchemaContents(final Resource resource) {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return IOUtils.toString(reader);
        } catch (IOException e) {
            log.error("An error occurred trying to read resource {}", resource, e);
        }
        return null;
    }

    private Resource getResource(final String schema) {
        return getResourceWithBackup(schema, getSchemaPath(schema));
    }

    private Resource getResource(final String namespace, final String schema) {
        return getResourceWithBackup(getSchemaPath(namespace, schema), schema);
    }

    private Resource getResourceWithBackup(final String firstTryKey, final String secondTryKey) {
        final Resource firstTry = schemas.get(firstTryKey);
        return firstTry != null ? firstTry : schemas.get(secondTryKey);
    }

    @Nullable
    private String getSchemaContentsFromDB(final String namespace, final String schema) {
        if (_dbBackedSchemaService == null) {
            log.debug("DBBackedSchemaService not available, skipping DB lookup for namespace={}, schema={}", namespace, schema);
            return null;
        }
        final String baseName = StringUtils.removeEnd(schema, ".xsd");
        for (final String candidatePath : new String[]{
                "/" + namespace + "/" + baseName,
                "/" + namespace + "/" + schema,
                "/" + baseName
        }) {
            log.debug("Trying DB schema lookup by path: {}", candidatePath);
            final DBBackedSchema dbSchema = _dbBackedSchemaService.findConfigByPath(candidatePath);
            if (dbSchema != null && StringUtils.isNotBlank(dbSchema.getContent())) {
                log.debug("Found DB-backed schema by path={} (name={}, id={})", candidatePath, dbSchema.getName(), dbSchema.getId());
                return dbSchema.getContent();
            }
        }
        // Also try lookup by schema name (e.g., "prefix:complexType") since
        // GetAllSchemaLocations uses the XFTDataModel fileName as the URL path
        // segment, which is the schema name for DB-backed schemas.
        for (final String candidateName : new String[]{namespace, baseName, schema}) {
            log.debug("Trying DB schema lookup by name: {}", candidateName);
            final DBBackedSchema dbSchema = _dbBackedSchemaService.findConfigByName(candidateName);
            if (dbSchema != null && StringUtils.isNotBlank(dbSchema.getContent())) {
                log.debug("Found DB-backed schema by name={} (path={}, id={})", candidateName, dbSchema.getPath(), dbSchema.getId());
                return dbSchema.getContent();
            }
        }
        log.debug("No DB-backed schema found for namespace={}, schema={}", namespace, schema);
        return null;
    }

    @Nullable
    private Document getSchemaDocFromDB(final String namespace, final String schema) {
        final String content = getSchemaContentsFromDB(namespace, schema);
        if (content == null) {
            return null;
        }
        try {
            return documentBuilder.parse(new InputSource(new java.io.StringReader(content)));
        } catch (SAXException | IOException e) {
            log.error("Error parsing DB-backed schema content for {}/{}", namespace, schema, e);
            return null;
        }
    }

    private final Map<String, Resource> schemas;
    private final DocumentBuilder documentBuilder;
    private       DBBackedSchemaService _dbBackedSchemaService;
}
