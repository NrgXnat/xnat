/*
 * web: org.nrg.xnat.services.CreateDataTypeCallable
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2025, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.XFTSchema;
import org.nrg.xft.schema.db.entities.DBBackedSchema;
import org.nrg.xft.schema.db.services.DBBackedSchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Callable for creating a new data type in XNAT.
 * This encapsulates the logic for validating and creating a new data type element.
 */
public class CreateDataTypeCallable implements Callable<CreateDataTypeResult> {
    public static final String SCHEMA_ID = "schemaId";

    private static final Logger       logger             = LoggerFactory.getLogger(CreateDataTypeCallable.class);
    private static final List<String> ALLOWED_EXTENSIONS = Lists.newArrayList("subjectAssessorData", "imageAssessorData", "abstractProjectAsset");

    private final String prefix;
    private final String type;
    private String name;
    private final String singular;
    private final String plural;
    private final String extensionS;

    /**
     * Constructor for CreateDataTypeCallable
     *
     * @param prefix     Schema prefix (can be null, will auto-generate)
     * @param type       Complex type name (can be null, will derive from name)
     * @param name       Backend name of the data type
     * @param singular   Singular display name
     * @param plural     Plural display name
     * @param extensionS The base type to extend (e.g., "subjectAssessorData", "imageAssessorData")
     */
    public CreateDataTypeCallable(String prefix, String type, String name, String singular, String plural, String extensionS) {
        this.prefix = prefix;
        this.type = type;
        this.name = name;
        this.singular = singular;
        this.plural = plural;
        this.extensionS = extensionS;
    }

    @Override
    public CreateDataTypeResult call() throws Exception {
        // Validate required fields
        if (StringUtils.isBlank(name) || StringUtils.isBlank(extensionS) ||
                StringUtils.isBlank(singular) || StringUtils.isBlank(plural)) {
            return CreateDataTypeResult.badRequest("Missing required fields");
        }

        logger.debug("Being asked to create schema with name {} extension {} singular {} plural {}", name, extensionS, singular, plural);
        String finalPrefix = prefix;
        if (finalPrefix == null) {
            finalPrefix = nextSchemaPrefix();
        }

        String finalType = type;
        if (finalType == null) {
            if (name.endsWith("Data") && name.length() > 4) {
                // User already included "Data" suffix — use as-is for complexType
                finalType = name.substring(0, 1).toLowerCase() + name.substring(1);
                name = name.substring(0, name.length() - 4);
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
            } else {
                finalType = name + "Data";
                if (StringUtils.isAllUpperCase(name.substring(0, 1))) {
                    finalType = name.substring(0, 1).toLowerCase() + finalType.substring(1);
                } else {
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                }
            }
        }

        // Validate character restrictions: only letters, numbers, and underscores
        String validCharPattern = "^[A-Za-z0-9_]+$";
        String validCharPatternWithSpaces = "^[A-Za-z0-9_\\s]+$";

        if (!name.matches(validCharPattern)) {
            return CreateDataTypeResult.badRequest("Name can only contain letters, numbers, and underscores");
        }

        if (!finalPrefix.matches(validCharPattern)) {
            return CreateDataTypeResult.badRequest("Prefix can only contain letters, numbers, and underscores");
        }

        if (!finalType.matches(validCharPattern)) {
            return CreateDataTypeResult.badRequest("Complex type can only contain letters, numbers, and underscores");
        }

        // Validate prefix does not start with a number
        if (finalPrefix.length() > 0 && Character.isDigit(finalPrefix.charAt(0))) {
            return CreateDataTypeResult.badRequest("Prefix cannot start with a number");
        }

        // Validate type does not start with a number
        if (finalType.length() > 0 && Character.isDigit(finalType.charAt(0))) {
            return CreateDataTypeResult.badRequest("Type cannot start with a number");
        }

        if (!singular.matches(validCharPatternWithSpaces)) {
            return CreateDataTypeResult.badRequest("Singular name can only contain letters, numbers, underscores, and spaces");
        }

        if (!plural.matches(validCharPatternWithSpaces)) {
            return CreateDataTypeResult.badRequest("Plural name can only contain letters, numbers, underscores, and spaces");
        }

        // Validate name length: 2-24 characters
        if (name.length() < 2 || name.length() > 24) {
            return CreateDataTypeResult.badRequest("Name must be between 2 and 24 characters");
        }

        // Validate singular name length: 2-24 characters
        if (singular.length() < 2 || singular.length() > 24) {
            return CreateDataTypeResult.badRequest("Singular name must be between 2 and 24 characters");
        }

        // Validate plural name length: 2-24 characters
        if (plural.length() < 2 || plural.length() > 24) {
            return CreateDataTypeResult.badRequest("Plural name must be between 2 and 24 characters");
        }

        if (!ALLOWED_EXTENSIONS.contains(extensionS)) {
            return CreateDataTypeResult.forbidden("Extension type not allowed");
        }

        SchemaElement extension;
        try {
            extension = SchemaElement.GetElement("xnat:" + extensionS);
        } catch (XFTInitException e) {
            logger.error("", e);
            return CreateDataTypeResult.internalError("Failed to initialize schema");
        } catch (ElementNotFoundException e) {
            return CreateDataTypeResult.badRequest("Extension element not found");
        }

        final String newComplexType = finalPrefix + ":" + finalType;

        // Confirm it doesn't exist
        try {
            SchemaElement.GetElement(newComplexType);
            return CreateDataTypeResult.conflict("Data type already exists");
        } catch (XFTInitException e) {
            logger.error("", e);
            return CreateDataTypeResult.internalError("Failed to check for existing element");
        } catch (ElementNotFoundException e) {
            // Ignore, this is correct - element should not exist
        }

        DBBackedSchemaService dbService = XFTManager.getDBBackedSchemaService();
        dbService.registerNewElement(finalPrefix, finalType, name, extension, singular, plural, true);

        // Register Element Security
        try {
            final ElementSecurity elementSecurity = ElementSecurity.newElementSecurity(newComplexType, name);
            elementSecurity.setPlural(plural);
            elementSecurity.setSingular(singular);
            elementSecurity.save(null, true, false, null);

            // Initialize existing group permissions
            elementSecurity.initExistingPermissions();

            // Refresh settings
            ElementSecurity.refresh();

            final DBBackedSchema schema = dbService.findConfigByName(newComplexType);
            XDAT.triggerXftItemEvent(ElementSecurity.SCHEMA_ELEMENT_NAME, elementSecurity.getElementName(), XftItemEvent.CREATE, Map.of(SCHEMA_ID, schema.getId()));
            return CreateDataTypeResult.success(newComplexType);
        } catch (Exception e) {
            logger.error("", e);
            return CreateDataTypeResult.internalError("Failed to register element security");
        }
    }

    private String nextSchemaPrefix() {
        int customPrefixCounter = 0;
        String targetPrefix;
        boolean prefixExists;

        do {
            targetPrefix = "x" + customPrefixCounter;
            prefixExists = false;

            // Check if this prefix already exists in the schemas
            for (final XFTSchema schema : XFTManager.GetSchemas()) {
                if (StringUtils.equals(schema.getTargetNamespacePrefix(), (targetPrefix))) {
                    prefixExists = true;
                    break;
                }
            }

            if (prefixExists) {
                customPrefixCounter++;
            }
        } while (prefixExists);

        return targetPrefix;
    }
}
