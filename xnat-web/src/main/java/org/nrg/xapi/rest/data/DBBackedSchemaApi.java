/*
 * web: org.nrg.xapi.rest.data.DBBackedSchemaApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2025, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.display.transport.entities.ElementDisplayDB;
import org.nrg.xdat.display.transport.services.ElementDisplayStorageService;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.db.entities.DBBackedSchema;
import org.nrg.xft.schema.db.services.DBBackedSchemaService;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.services.LoadDBDataTypeCallable;
import org.nrg.xnat.services.LoadDBDataTypeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.xml.validation.Schema;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * XNAT DBBackedSchema API
 * Provides REST endpoints for managing database-backed schemas
 */
@XapiRestController
@RequestMapping(value = "/dbschemas")
@Slf4j
public class DBBackedSchemaApi extends AbstractXapiRestController {

    @Autowired
    public DBBackedSchemaApi(final UserManagementServiceI userManagementService,
                             final RoleHolder roleHolder,
                             final DBBackedSchemaService dbBackedSchemaService,
                             final ElementDisplayStorageService elementDisplayStorageService) {
        super(userManagementService, roleHolder);
        _dbBackedSchemaService = dbBackedSchemaService;
        _elementDisplayStorageService = elementDisplayStorageService;
    }

    /**
     * Gets all database-backed schemas.
     * Returns a list of all DBBackedSchema entries in the database. Requires administrator privileges.
     *
     * @return List of all database-backed schemas
     */
    @XapiRequestMapping(value = "", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public List<Map<String, Object>> getAllSchemas() {
        log.debug("User {} is requesting all database-backed schemas", getSessionUser().getUsername());
        return _dbBackedSchemaService.findAllSchema().stream()
                .map(schema -> {
                    // Duplicate the schema payload so we can append the related elements before serializing.
                    Map<String, Object> schemaJson = OBJECT_MAPPER.convertValue(schema, SCHEMA_MAP_TYPE);
                    schemaJson.put("elements", _dbBackedSchemaService.getElementNames(schema));
                    return schemaJson;
                })
                .collect(Collectors.toList());
    }

    /**
     * Deletes a database-backed schema.
     * First deletes all associated ElementSecurity and ElementDisplay entries for elements in this schema,
     * then deletes the schema itself. Requires administrator privileges.
     *
     * @param id The ID of the schema to delete
     * @return ResponseEntity indicating success or failure
     */
    @XapiRequestMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = Admin)
    public ResponseEntity<String> deleteSchema(@PathVariable Long id) {
        log.debug("User {} is requesting to delete database-backed schema with ID: {}", getSessionUser().getUsername(), id);

        try {
            // Get the schema by ID
            DBBackedSchema schema = _dbBackedSchemaService.get(id);
            if (schema == null) {
                log.warn("Schema with ID {} not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Schema not found with ID: " + id + "\"}");
            }

            // Get all element names from this schema
            List<String> elementNames = _dbBackedSchemaService.getElementNames(schema);
            log.debug("Found {} elements in schema {}: {}", elementNames.size(), schema.getName(), elementNames);

            // Check for experiments
            for (String elementName : elementNames) {
                GenericWrapperElement se = GenericWrapperElement.GetElement(elementName);
                Long n = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM "+se.getSQLName(),"COUNT", null, null);
                if(n!=null && n.intValue()>0){
                    Exception e = new Exception("Schema cannot be deleted because it contains experiments.");
                    log.error("Error deleting schema with ID: " + id,e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("{\"error\":\"Failed to delete schema: " + e.getMessage() + "\"}");
                }
            }

            // Delete ElementSecurity for each element - stop on first failure
            Map<String, ElementSecurity> allElementSecurities = ElementSecurity.GetElementSecurities();
            for (String elementName : elementNames) {
                ElementSecurity elementSecurity = allElementSecurities.get(elementName);
                if (elementSecurity != null) {
                    log.debug("Deleting ElementSecurity for element: {}", elementName);
                    SaveItemHelper.authorizedDelete(elementSecurity.getItem(), getSessionUser(), EventUtils.ADMIN_EVENT(getSessionUser()));
                } else {
                    log.debug("No ElementSecurity found for element: {}", elementName);
                }
            }

            // Delete ElementDisplay for each element - stop on first failure
            for (String elementName : elementNames) {
                ElementDisplayDB elementDisplay = _elementDisplayStorageService.findByElementName(elementName);
                if (elementDisplay != null) {
                    log.debug("Deleting ElementDisplay for element: {}", elementName);
                    _elementDisplayStorageService.delete(elementDisplay);

                    DisplayManager.GetInstance().getElements().remove(elementDisplay.getElementName());
                } else {
                    log.debug("No ElementDisplay found for element: {}", elementName);
                }
            }

            // Delete the schema itself
            log.debug("Deleting schema: {}", schema.getName());
            _dbBackedSchemaService.delete(schema);

            log.info("Successfully deleted schema with ID: {} (name: {})", id, schema.getName());
            return ResponseEntity.ok("{\"message\":\"Schema deleted successfully\"}");

        } catch (Exception e) {
            log.error("Error deleting schema with ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Failed to delete schema: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Loads a database-backed schema into the runtime configuration.
     * Checks if the schema is already loaded before attempting to load it.
     * This is used when a schema has been created on another server and needs to be
     * activated on this XNAT instance without creating database objects.
     *
     * @param id The ID of the schema to load
     * @return ResponseEntity indicating success or failure
     */
    @XapiRequestMapping(value = "/{id}/load", produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Admin)
    public ResponseEntity<String> loadSchema(@PathVariable Long id) {
        log.debug("User {} is requesting to load database-backed schema with ID: {}", getSessionUser().getUsername(), id);

        try {
            // Get the schema by ID
            DBBackedSchema schema = _dbBackedSchemaService.get(id);
            if (schema == null) {
                log.warn("Schema with ID {} not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Schema not found with ID: " + id + "\"}");
            }

            // Get element names from the schema
            List<String> elementNames = _dbBackedSchemaService.getElementNames(schema);
            log.debug("Schema {} contains {} elements: {}", schema.getName(), elementNames.size(), elementNames);

            // Check if any element from this schema is already loaded
            boolean alreadyLoaded = false;
            for (String elementName : elementNames) {
                try {
                    SchemaElement se = SchemaElement.GetElement(elementName);
                    if (se != null) {
                        log.info("Element {} is already loaded in the system", elementName);
                        alreadyLoaded = true;
                        break;
                    }
                } catch (XFTInitException e) {
                    log.error("XFT initialization error checking for element: " + elementName, e);
                } catch (ElementNotFoundException e) {
                    // Element not found, which is expected if not loaded yet
                    log.debug("Element {} not yet loaded", elementName);
                }
            }

            if (alreadyLoaded) {
                log.info("Schema {} is already loaded in the system", schema.getName());
                return ResponseEntity.ok("{\"message\":\"Schema is already loaded\",\"status\":\"already_loaded\"}");
            }

            // Load the schema using LoadDBDataTypeCallable
            log.info("Loading schema {} into runtime configuration", schema.getName());
            LoadDBDataTypeCallable callable = new LoadDBDataTypeCallable(id, _dbBackedSchemaService, _elementDisplayStorageService);
            LoadDBDataTypeResult result = callable.call();

            if (result.isSuccess()) {
                log.info("Successfully loaded schema with ID: {} (name: {})", id, schema.getName());
                return ResponseEntity.ok("{\"message\":\"" + result.getMessage() + "\",\"status\":\"loaded\"}");
            } else {
                log.error("Failed to load schema with ID: {}, status: {}", id, result.getStatus());
                HttpStatus httpStatus = result.getStatus() == LoadDBDataTypeResult.Status.NOT_FOUND
                        ? HttpStatus.NOT_FOUND
                        : HttpStatus.INTERNAL_SERVER_ERROR;
                return ResponseEntity.status(httpStatus)
                        .body("{\"error\":\"" + result.getMessage() + "\"}");
            }

        } catch (Exception e) {
            log.error("Error loading schema with ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Failed to load schema: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Downloads the schema XSD content for a database-backed schema.
     * Returns the XSD schema content as XML for download.
     *
     * @param id The ID of the schema to download
     * @return ResponseEntity with schema XSD content
     */
    @XapiRequestMapping(value = "/{id}/schema", produces = APPLICATION_XML_VALUE, method = GET, restrictTo = Admin)
    public ResponseEntity<String> downloadSchema(@PathVariable Long id) {
        log.debug("User {} is requesting to download schema XSD for ID: {}", getSessionUser().getUsername(), id);

        try {
            DBBackedSchema schema = _dbBackedSchemaService.get(id);
            if (schema == null) {
                log.warn("Schema with ID {} not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("<?xml version=\"1.0\"?><error>Schema not found with ID: " + id + "</error>");
            }

            String content = schema.getContent();
            if (content == null || content.trim().isEmpty()) {
                log.warn("Schema with ID {} has no content", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("<?xml version=\"1.0\"?><error>Schema has no content</error>");
            }

            log.debug("Returning schema XSD for: {}", schema.getName());
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + schema.getName() + ".xsd\"")
                    .body(content);

        } catch (Exception e) {
            log.error("Error downloading schema with ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("<?xml version=\"1.0\"?><error>Failed to download schema: " + e.getMessage() + "</error>");
        }
    }

    private final DBBackedSchemaService _dbBackedSchemaService;
    private final ElementDisplayStorageService _elementDisplayStorageService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> SCHEMA_MAP_TYPE = new TypeReference<Map<String, Object>>() {};
}
