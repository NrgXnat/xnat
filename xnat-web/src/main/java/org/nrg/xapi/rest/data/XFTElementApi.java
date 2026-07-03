/*
 * web: org.nrg.xapi.rest.data.XFTElementApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2025, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.data;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.references.XFTManyToManyReference;
import org.nrg.xft.references.XFTReferenceI;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.references.XFTSuperiorReference;
import org.nrg.xft.schema.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.nrg.xdat.security.helpers.AccessLevel.Authenticated;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * XNAT XFT Element API
 * Provides read-only REST endpoints for accessing internal XFT configuration held in memory
 */
@Tag(name = "XNAT XFT Element API")
@XapiRestController
@RequestMapping(value = "/xft")
@Slf4j
public class XFTElementApi extends AbstractXapiRestController {

    // Protected namespace prefixes that should not be exposed via API
    private static final String PROTECTED_PREFIX = "xdat";
    private static final Set<String> PROTECTED_ELEMENT_PREFIXES = new HashSet<>(Arrays.asList("xdat:", "arc:", "cat:"));

    @Autowired
    public XFTElementApi(final UserManagementServiceI userManagementService,
                         final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
    }

    @Operation(summary = "Get all XFT schemas", description = "Returns a list of all XFT schemas loaded in memory. Excludes protected namespaces.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved schemas"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @XapiRequestMapping(value = "/schemas", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public ResponseEntity<List<Map<String, Object>>> getSchemas() {
        log.debug("User {} is requesting XFT schemas list", getSessionUser().getUsername());

        try {
            List<Map<String, Object>> schemas = new ArrayList<>();

            for (XFTSchema schema : XFTManager.GetSchemas()) {
                // Skip protected namespace
                if (PROTECTED_PREFIX.equals(schema.getTargetNamespacePrefix())) {
                    continue;
                }

                Map<String, Object> schemaInfo = new HashMap<>();
                schemaInfo.put("targetNamespacePrefix", schema.getTargetNamespacePrefix());
                schemaInfo.put("targetNamespaceURI", schema.getTargetNamespaceURI());
                schemaInfo.put("fileName", schema.getDataModel() != null ? schema.getDataModel().getFileName() : "");
                schemaInfo.put("elementCount", schema.getElementsByName().size());

                schemas.add(schemaInfo);
            }

            return ResponseEntity.ok(schemas);
        } catch (Exception e) {
            log.error("Error retrieving XFT schemas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get all meta element names", description = "Returns a list of all XFT element names from XFTMetaManager. By default excludes history and metadata elements.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved element names"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @XapiRequestMapping(value = "/elements", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public ResponseEntity<List<String>> getMetaElementNames(
            @Parameter(description = "Include metadata elements (ending in _meta_data)", required = false)
            @RequestParam(value = "showMeta", required = false, defaultValue = "false") boolean showMeta,
            @Parameter(description = "Include history elements (ending in _history)", required = false)
            @RequestParam(value = "showHistory", required = false, defaultValue = "false") boolean showHistory) {

        log.debug("User {} is requesting XFT meta element names list (showMeta={}, showHistory={})",
                  getSessionUser().getUsername(), showMeta, showHistory);

        try {
            ArrayList elementNames = XFTMetaManager.GetElementNames();
            List<String> names = new ArrayList<>();

            for (Object name : elementNames) {
                if (name instanceof String) {
                    String elementName = (String) name;

                    // Filter out protected namespace elements (xdat:, arc:, cat:)
                    boolean isProtected = false;
                    for (String protectedPrefix : PROTECTED_ELEMENT_PREFIXES) {
                        if (elementName.startsWith(protectedPrefix)) {
                            isProtected = true;
                            break;
                        }
                    }
                    if (isProtected) {
                        continue;
                    }

                    // Filter out metadata elements unless showMeta is true
                    if (!showMeta && elementName.endsWith("_meta_data")) {
                        continue;
                    }

                    // Filter out history elements unless showHistory is true
                    if (!showHistory && elementName.endsWith("_history")) {
                        continue;
                    }

                    names.add(elementName);
                }
            }

            // Sort alphabetically (case-insensitive)
            names.sort(String.CASE_INSENSITIVE_ORDER);

            return ResponseEntity.ok(names);
        } catch (Exception e) {
            log.error("Error retrieving XFT meta element names", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get all XFT references", description = "Returns a list of all XFT references from XFTReferenceManager, including many-to-many and superior references.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved references"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @XapiRequestMapping(value = "/references", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public ResponseEntity<List<Map<String, Object>>> getAllXFTReferences() {
        log.debug("User {} is requesting XFT references list", getSessionUser().getUsername());

        try {
            List<XFTReferenceI> references = XFTReferenceManager.GetInstance().getAllXFTReferences();
            List<Map<String, Object>> referencesList = new ArrayList<>();

            for (XFTReferenceI reference : references) {
                // Skip references involving protected namespace
                boolean isProtected = false;

                if (reference instanceof XFTManyToManyReference) {
                    XFTManyToManyReference m2m = (XFTManyToManyReference) reference;
                    if ((m2m.getElement1() != null && m2m.getElement1().getFullXMLName().startsWith(PROTECTED_PREFIX + ":")) ||
                        (m2m.getElement2() != null && m2m.getElement2().getFullXMLName().startsWith(PROTECTED_PREFIX + ":"))) {
                        isProtected = true;
                    }
                } else if (reference instanceof XFTSuperiorReference) {
                    XFTSuperiorReference superior = (XFTSuperiorReference) reference;
                    if ((superior.getSuperiorElement() != null && superior.getSuperiorElement().getFullXMLName().startsWith(PROTECTED_PREFIX + ":")) ||
                        (superior.getSubordinateElement() != null && superior.getSubordinateElement().getFullXMLName().startsWith(PROTECTED_PREFIX + ":"))) {
                        isProtected = true;
                    }
                }

                if (isProtected) {
                    continue; // Skip this reference
                }

                Map<String, Object> refInfo = new HashMap<>();

                refInfo.put("manyToMany", reference.isManyToMany());

                if (reference instanceof XFTManyToManyReference) {
                    XFTManyToManyReference m2m = (XFTManyToManyReference) reference;
                    refInfo.put("referenceType", "manyToMany");
                    refInfo.put("mappingTable", m2m.getMappingTable());

                    if (m2m.getElement1() != null) {
                        Map<String, String> element1Info = new HashMap<>();
                        element1Info.put("name", m2m.getElement1().getFullXMLName());
                        element1Info.put("sqlName", m2m.getElement1().getSQLName());
                        refInfo.put("element1", element1Info);
                    }

                    if (m2m.getElement2() != null) {
                        Map<String, String> element2Info = new HashMap<>();
                        element2Info.put("name", m2m.getElement2().getFullXMLName());
                        element2Info.put("sqlName", m2m.getElement2().getSQLName());
                        refInfo.put("element2", element2Info);
                    }

                    if (m2m.getField1() != null) {
                        Map<String, String> field1Info = new HashMap<>();
                        field1Info.put("name", m2m.getField1().getName());
                        field1Info.put("xmlPath", m2m.getField1().getXMLPathString(""));
                        refInfo.put("field1", field1Info);
                    }

                    if (m2m.getField2() != null) {
                        Map<String, String> field2Info = new HashMap<>();
                        field2Info.put("name", m2m.getField2().getName());
                        field2Info.put("xmlPath", m2m.getField2().getXMLPathString(""));
                        refInfo.put("field2", field2Info);
                    }

                } else if (reference instanceof XFTSuperiorReference) {
                    XFTSuperiorReference superior = (XFTSuperiorReference) reference;
                    refInfo.put("referenceType", "superior");
                    refInfo.put("superiorElementName", superior.getSuperiorElementName());
                    refInfo.put("subordinateElementName", superior.getSubordinateElementName());

                    if (superior.getSuperiorElement() != null) {
                        Map<String, String> superiorElementInfo = new HashMap<>();
                        superiorElementInfo.put("name", superior.getSuperiorElement().getFullXMLName());
                        superiorElementInfo.put("sqlName", superior.getSuperiorElement().getSQLName());
                        refInfo.put("superiorElement", superiorElementInfo);
                    }

                    if (superior.getSubordinateElement() != null) {
                        Map<String, String> subordinateElementInfo = new HashMap<>();
                        subordinateElementInfo.put("name", superior.getSubordinateElement().getFullXMLName());
                        subordinateElementInfo.put("sqlName", superior.getSubordinateElement().getSQLName());
                        refInfo.put("subordinateElement", subordinateElementInfo);
                    }

                    if (superior.getSuperiorField() != null) {
                        Map<String, String> superiorFieldInfo = new HashMap<>();
                        superiorFieldInfo.put("name", superior.getSuperiorField().getName());
                        superiorFieldInfo.put("xmlPath", superior.getSuperiorField().getXMLPathString(""));
                        superiorFieldInfo.put("sqlName", superior.getSuperiorFieldSQLName());
                        refInfo.put("superiorField", superiorFieldInfo);
                    }

                    if (superior.getSubordinateField() != null) {
                        Map<String, String> subordinateFieldInfo = new HashMap<>();
                        subordinateFieldInfo.put("name", superior.getSubordinateField().getName());
                        subordinateFieldInfo.put("xmlPath", superior.getSubordinateField().getXMLPathString(""));
                        subordinateFieldInfo.put("sqlName", superior.getSubordinateFieldSQLName());
                        refInfo.put("subordinateField", subordinateFieldInfo);
                    }
                }

                referencesList.add(refInfo);
            }

            return ResponseEntity.ok(referencesList);
        } catch (Exception e) {
            log.error("Error retrieving XFT references", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get elements from a schema", description = "Returns all XFT elements for all schemas with the specified target namespace prefix. Multiple schemas can share the same prefix. Protected namespaces are not accessible. By default excludes history and metadata elements.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved elements"),
        @ApiResponse(responseCode = "403", description = "Access to protected namespace is forbidden"),
        @ApiResponse(responseCode = "404", description = "Schema not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @XapiRequestMapping(value = "/schemas/{prefix}/elements", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public ResponseEntity<List<Map<String, Object>>> getSchemaElements(
            @Parameter(description = "Target namespace prefix of the schema", required = true)
            @PathVariable String prefix,
            @Parameter(description = "Include metadata elements (ending in _meta_data)", required = false)
            @RequestParam(value = "showMeta", required = false, defaultValue = "false") boolean showMeta,
            @Parameter(description = "Include history elements (ending in _history)", required = false)
            @RequestParam(value = "showHistory", required = false, defaultValue = "false") boolean showHistory) {

        log.debug("User {} is requesting elements for schema: {} (showMeta={}, showHistory={})",
                  getSessionUser().getUsername(), prefix, showMeta, showHistory);

        // Check for protected namespace
        if (PROTECTED_PREFIX.equals(prefix)) {
            log.warn("User {} attempted to access protected namespace: {}", getSessionUser().getUsername(), prefix);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<XFTSchema> schemas = findSchemasByPrefix(prefix);
            if (schemas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            List<Map<String, Object>> elements = new ArrayList<>();

            // Collect elements from all schemas with this prefix
            for (XFTSchema schema : schemas) {
                for (Map.Entry<String, XFTElement> entry : (Set<Map.Entry<String, XFTElement>>)schema.getElementsByName().entrySet()) {
                    XFTElement element = entry.getValue();
                    String elementName = element.getName();

                    // Filter out metadata elements unless showMeta is true
                    if (!showMeta && elementName.endsWith("_meta_data")) {
                        continue;
                    }

                    // Filter out history elements unless showHistory is true
                    if (!showHistory && elementName.endsWith("_history")) {
                        continue;
                    }

                    Map<String, Object> elementInfo = buildElementInfo(element);
                    elements.add(elementInfo);
                }
            }

            // Sort elements alphabetically by Type (fullXMLName)
            elements.sort((e1, e2) -> {
                String type1 = (String) e1.getOrDefault("fullXMLName", "");
                String type2 = (String) e2.getOrDefault("fullXMLName", "");
                return type1.compareToIgnoreCase(type2);
            });

            return ResponseEntity.ok(elements);
        } catch (Exception e) {
            log.error("Error retrieving elements for schema: " + prefix, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get a specific XFT element", description = "Returns detailed information about a specific XFT element including basic metadata. Searches across all schemas with the specified prefix. Protected namespaces are not accessible.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved element"),
        @ApiResponse(responseCode = "403", description = "Access to protected namespace is forbidden"),
        @ApiResponse(responseCode = "404", description = "Element not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @XapiRequestMapping(value = "/schemas/{prefix}/elements/{elementName}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public ResponseEntity<Map<String, Object>> getElement(
            @Parameter(description = "Target namespace prefix of the schema", required = true)
            @PathVariable String prefix,
            @Parameter(description = "Name of the element", required = true)
            @PathVariable String elementName) {

        log.debug("User {} is requesting element: {} from schema: {}", getSessionUser().getUsername(), elementName, prefix);

        // Check for protected namespace
        if (PROTECTED_PREFIX.equals(prefix)) {
            log.warn("User {} attempted to access protected namespace: {}", getSessionUser().getUsername(), prefix);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<XFTSchema> schemas = findSchemasByPrefix(prefix);
            if (schemas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Search for element across all schemas with this prefix
            XFTElement element = null;
            for (XFTSchema schema : schemas) {
                element = (XFTElement) schema.getElementsByName().get(elementName.toLowerCase());
                if (element != null) {
                    break;
                }
            }

            if (element == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Map<String, Object> elementInfo = buildElementInfo(element);

            return ResponseEntity.ok(elementInfo);
        } catch (Exception e) {
            log.error("Error retrieving element: " + elementName + " from schema: " + prefix, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get fields from an element", description = "Returns all fields (both data fields and reference fields) for a specific XFT element. Searches across all schemas with the specified prefix. Protected namespaces are not accessible.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved fields"),
        @ApiResponse(responseCode = "403", description = "Access to protected namespace is forbidden"),
        @ApiResponse(responseCode = "404", description = "Element not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @XapiRequestMapping(value = "/schemas/{prefix}/elements/{elementName}/fields", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public ResponseEntity<List<Map<String, Object>>> getElementFields(
            @Parameter(description = "Target namespace prefix of the schema", required = true)
            @PathVariable String prefix,
            @Parameter(description = "Name of the element", required = true)
            @PathVariable String elementName) {

        log.debug("User {} is requesting fields for element: {} from schema: {}",
                  getSessionUser().getUsername(), elementName, prefix);

        // Check for protected namespace
        if (PROTECTED_PREFIX.equals(prefix)) {
            log.warn("User {} attempted to access protected namespace: {}", getSessionUser().getUsername(), prefix);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<XFTSchema> schemas = findSchemasByPrefix(prefix);
            if (schemas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Search for element across all schemas with this prefix
            XFTElement element = null;
            for (XFTSchema schema : schemas) {
                element = (XFTElement) schema.getElementsByName().get(elementName.toLowerCase());
                if (element != null) {
                    break;
                }
            }

            if (element == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            List<Map<String, Object>> fields = new ArrayList<>();

            for (Map.Entry<String, XFTField> entry : (Set<Map.Entry<String, XFTField>>)element.getFields().entrySet()) {
                XFTField field = entry.getValue();
                addFieldWithChildren(fields, field, "");
            }

            return ResponseEntity.ok(fields);
        } catch (Exception e) {
            log.error("Error retrieving fields for element: " + elementName + " from schema: " + prefix, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Helper method to find all schemas by their target namespace prefix.
     * Multiple schemas can share the same prefix.
     * Protected namespaces are excluded.
     */
    private List<XFTSchema> findSchemasByPrefix(String prefix) {
        // Don't return protected namespaces
        if (PROTECTED_PREFIX.equals(prefix)) {
            return new ArrayList<>();
        }

        List<XFTSchema> matchingSchemas = new ArrayList<>();
        for (XFTSchema schema : XFTManager.GetSchemas()) {
            if (prefix.equals(schema.getTargetNamespacePrefix())) {
                matchingSchemas.add(schema);
            }
        }
        return matchingSchemas;
    }

    /**
     * Helper method to build element information map
     */
    private Map<String, Object> buildElementInfo(XFTElement element) {
        Map<String, Object> elementInfo = new HashMap<>();

        elementInfo.put("name", element.getName());
        elementInfo.put("code", element.getCode());
        elementInfo.put("briefDescription", element.getBriefDescription());
        elementInfo.put("fullDescription", element.getFullDescription());
        elementInfo.put("xsDescription", element.getXsDescription());
        elementInfo.put("extension", element.isExtension());
        elementInfo.put("abstract", element.isAbstract());
        elementInfo.put("skipSQL", element.isSkipSQL());
        elementInfo.put("matchByValues", element.isMatchByValues());
        elementInfo.put("addin", element.getAddin());
        elementInfo.put("sequence", element.getSequence());
        elementInfo.put("fieldCount", element.getFields().size());

        // Include type information
        if (element.getType() != null) {
            Map<String, String> typeInfo = new HashMap<>();
            typeInfo.put("localType", element.getType().getLocalType());
            typeInfo.put("fullLocalType", element.getType().getFullLocalType());
            typeInfo.put("fullForeignType", element.getType().getFullForeignType());
            elementInfo.put("type", typeInfo);
            // fullForeignType serves as the full XML name
            elementInfo.put("fullXMLName", element.getType().getFullForeignType());
        }

        // Include extension type if present
        if (element.getExtensionType() != null) {
            Map<String, String> extensionTypeInfo = new HashMap<>();
            extensionTypeInfo.put("localType", element.getExtensionType().getLocalType());
            extensionTypeInfo.put("fullLocalType", element.getExtensionType().getFullLocalType());
            extensionTypeInfo.put("fullForeignType", element.getExtensionType().getFullForeignType());
            elementInfo.put("extensionType", extensionTypeInfo);
        }

        return elementInfo;
    }

    /**
     * Helper method to build field information map
     */
    private Map<String, Object> buildFieldInfo(XFTField field) {
        Map<String, Object> fieldInfo = new HashMap<>();

        // Common field properties
        fieldInfo.put("name", field.getName());
        fieldInfo.put("fullName", field.getFullName());
        fieldInfo.put("displayName", field.getDisplayName());
        fieldInfo.put("description", field.getDescription());
        fieldInfo.put("minOccurs", field.getMinOccurs());
        fieldInfo.put("maxOccurs", field.getMaxOccurs());
        fieldInfo.put("required", field.getRequired());
        fieldInfo.put("unique", field.getUnique());
        fieldInfo.put("size", field.getSize());
        fieldInfo.put("expose", field.getExpose());
        fieldInfo.put("sequence", field.getSequence());

        // Determine field type and add specific properties
        if (field instanceof XFTDataField) {
            fieldInfo.put("fieldType", "dataField");
            XFTDataField dataField = (XFTDataField) field;

            if (dataField.getXMLType() != null) {
                Map<String, String> typeInfo = new HashMap<>();
                typeInfo.put("localType", dataField.getXMLType().getLocalType());
                typeInfo.put("fullLocalType", dataField.getXMLType().getFullLocalType());
                typeInfo.put("fullForeignType", dataField.getXMLType().getFullForeignType());
                fieldInfo.put("xmlType", typeInfo);
            }

        } else if (field instanceof XFTReferenceField) {
            fieldInfo.put("fieldType", "referenceField");
            XFTReferenceField refField = (XFTReferenceField) field;

            if (refField.getXMLType() != null) {
                Map<String, String> typeInfo = new HashMap<>();
                typeInfo.put("localType", refField.getXMLType().getLocalType());
                typeInfo.put("fullLocalType", refField.getXMLType().getFullLocalType());
                typeInfo.put("fullForeignType", refField.getXMLType().getFullForeignType());
                fieldInfo.put("xmlType", typeInfo);
            }

            // Add reference information
            try {
                XFTElement refElement = refField.getRefElement();
                if (refElement != null) {
                    Map<String, Object> refElementInfo = new HashMap<>();
                    refElementInfo.put("name", refElement.getName());
                    if (refElement.getType() != null) {
                        refElementInfo.put("fullXMLName", refElement.getType().getFullForeignType());
                    }
                    fieldInfo.put("referenceElement", refElementInfo);
                }
            } catch (Exception e) {
                log.debug("Could not retrieve reference element for field: " + field.getName(), e);
            }

            // Add relationship type from XFTRelation
            if (field.getRelation() != null) {
                XFTRelation relation = field.getRelation();
                Map<String, Object> relationInfo = new HashMap<>();
                relationInfo.put("relationType", relation.getRelationType());
                relationInfo.put("onDelete", relation.getOnDelete());
                fieldInfo.put("relation", relationInfo);
            }
        }

        // SQL field information
        if (field.getSqlField() != null) {
            Map<String, Object> sqlFieldInfo = new HashMap<>();
            sqlFieldInfo.put("sqlName", field.getSqlField().getSqlName());
            sqlFieldInfo.put("defaultValue", field.getSqlField().getDefaultValue());
            fieldInfo.put("sqlField", sqlFieldInfo);
        }

        return fieldInfo;
    }

    /**
     * Recursively adds a field and all its child fields to the list.
     * For nested fields, concatenates parent path with field name using "/" separator.
     * Filters out fields that don't have a Type (xmlType is null).
     */
    private void addFieldWithChildren(List<Map<String, Object>> fields, XFTField field, String parentPath) {
        // Check if field has a type - skip fields without types
        boolean hasType = false;
        if (field instanceof XFTDataField) {
            hasType = ((XFTDataField) field).getXMLType() != null;
        } else if (field instanceof XFTReferenceField) {
            hasType = ((XFTReferenceField) field).getXMLType() != null;
        }

        if (!hasType) {
            return; // Skip this field and don't process its children
        }

        // Build the full field path
        String fieldPath = parentPath.isEmpty() ? field.getName() : parentPath + "/" + field.getName();

        // Build field info with the full path as the name
        Map<String, Object> fieldInfo = buildFieldInfo(field);
        fieldInfo.put("name", fieldPath);
        fields.add(fieldInfo);

        // Recursively add child fields if they exist
        if (field.getChildFields() != null && !field.getChildFields().isEmpty()) {
            for (Object childField : field.getChildFields().values()) {
                addFieldWithChildren(fields, (XFTField)childField, fieldPath);
            }
        }
    }
}
