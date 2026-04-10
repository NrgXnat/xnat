/*
 * web: org.nrg.xnat.services.LoadDBDataTypeCallable
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2025, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services;

import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.display.transport.services.ElementDisplayStorageService;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xft.schema.XFTDataModel;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.db.entities.DBBackedSchema;
import org.nrg.xft.schema.db.services.DBBackedSchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Callable for loading an existing database-backed data type into XNAT's runtime configuration.
 * This is used when the data type has already been created on another server or in the database,
 * and this XNAT instance just needs to load the schema and display configuration into memory
 * without creating any new database tables or objects.
 */
public class LoadDBDataTypeCallable implements Callable<LoadDBDataTypeResult> {
    private static final Logger logger = LoggerFactory.getLogger(LoadDBDataTypeCallable.class);

    private final Long schemaId;
    private final DBBackedSchemaService dbBackedSchemaService;
    private final ElementDisplayStorageService elementDisplayStorageService;

    /**
     * Constructor for LoadDBDataTypeCallable
     *
     * @param schemaId                      The ID of the DBBackedSchema to load
     * @param dbBackedSchemaService         Service for retrieving the schema
     * @param elementDisplayStorageService  Service for retrieving element displays
     */
    public LoadDBDataTypeCallable(Long schemaId,
                                   DBBackedSchemaService dbBackedSchemaService,
                                   ElementDisplayStorageService elementDisplayStorageService) {
        this.schemaId = schemaId;
        this.dbBackedSchemaService = dbBackedSchemaService;
        this.elementDisplayStorageService = elementDisplayStorageService;
    }

    @Override
    public LoadDBDataTypeResult call() throws Exception {
        try {
            // Retrieve the schema from the database
            DBBackedSchema schema = dbBackedSchemaService.get(schemaId);
            if (schema == null) {
                return LoadDBDataTypeResult.notFound("Schema not found with ID: " + schemaId);
            }

            if (dbBackedSchemaService.isSchemaLoaded(schema)) {
                return LoadDBDataTypeResult.success("Schema already loaded: " + schema.getName());
            }

            logger.info("Loading schema: {} (ID: {})", schema.getName(), schemaId);

            // Load the schema into XFTManager
            XFTDataModel dataModel = dbBackedSchemaService.initializeSchema(schema);
            if (dataModel == null) {
                return LoadDBDataTypeResult.internalError("Failed to load schema into XFTManager");
            }

            // Get the prefix from the schema to manage add-ins
            String prefix = dataModel.getSchema().getTargetNamespacePrefix();

            if (prefix == null) {
                return LoadDBDataTypeResult.internalError("Could not determine schema prefix");
            }

            logger.debug("Managing add-ins for prefix: {}", prefix);
            XFTManager.GetInstance().manageAddins(prefix);

            // Get all element names from this schema
            List<String> elementNames = dbBackedSchemaService.getElementNames(schema);
            logger.debug("Found {} elements in schema: {}", elementNames.size(), elementNames);

            // Verify ElementDisplay configurations are loaded for each element
            for (String elementName : elementNames) {
                logger.debug("Verifying ElementDisplay for: {}", elementName);

                // GetElementDisplay should automatically load from storage if not already in memory
                org.nrg.xdat.display.ElementDisplay runtimeDisplay =
                    DisplayManager.GetElementDisplay(elementName);

                if (runtimeDisplay != null) {
                    logger.debug("ElementDisplay loaded for: {}", elementName);
                } else {
                    logger.debug("ElementDisplay not in memory for: {}, attempting to load from storage", elementName);

                    // Retrieve from storage
                    org.nrg.xdat.display.transport.entities.ElementDisplayDB storedDisplay =
                        elementDisplayStorageService.findByElementName(elementName);

                    if (storedDisplay != null) {
                        logger.debug("Found stored ElementDisplay for: {}, adding to DisplayManager", elementName);

                        // Save the stored display to make it available in DisplayManager
                        org.nrg.xdat.display.ElementDisplay ed = elementDisplayStorageService.renderElementDisplay(storedDisplay);
                        DisplayManager.GetInstance().addElement(ed);

                        logger.debug("ElementDisplay added to DisplayManager for: {}", elementName);
                    } else {
                        logger.warn("No stored ElementDisplay found for: {} - this may cause display issues", elementName);
                    }
                }
            }

            // Refresh ElementSecurity configurations
            logger.debug("Refreshing ElementSecurity configurations");
            ElementSecurity.refresh();

            logger.info("Successfully loaded schema: {}", schema.getName());
            return LoadDBDataTypeResult.success(schema.getName());

        } catch (Exception e) {
            logger.error("Error loading schema with ID: {}", schemaId, e);
            return LoadDBDataTypeResult.internalError("Failed to load schema: " + e.getMessage());
        }
    }
}
