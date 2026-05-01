/*
 * web: org.nrg.xapi.rest.data.DataTypesApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2025, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.data;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.ElementAction;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.CreateDataTypeCallable;
import org.nrg.xnat.services.CreateDataTypeResult;
import org.nrg.xnat.services.DataTypeInfo;
import org.nrg.xnat.services.GetDataTypesCallable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

import java.util.*;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.nrg.xdat.security.helpers.AccessLevel.Authenticated;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * XNAT Data Types API
 * Provides REST endpoints for managing XNAT data types
 */
@Api("XNAT Data Types API")
@XapiRestController
@RequestMapping(value = "/datatypes")
@Slf4j
public class DataTypesApi extends AbstractXapiRestController {

    @Autowired
    public DataTypesApi(final UserManagementServiceI userManagementService,
                        final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
    }

    @ApiOperation(value = "Get all data types",
                  notes = "Returns a list of all XNAT data types with their properties.")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Successfully retrieved data types"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    @XapiRequestMapping(value = "", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authenticated)
    public ResponseEntity<List<Map<String, Object>>> getDataTypes(
            @ApiParam(value = "Filter to only secured data types", required = false)
            @RequestParam(required = false) Boolean secured,

            @ApiParam(value = "Filter to only data types with existing data", required = false)
            @RequestParam(required = false) Boolean used,

            @ApiParam(value = "Use readable counts instead of total counts", required = false)
            @RequestParam(required = false) Boolean readable) {

        log.debug("User {} is requesting data types list", getSessionUser().getUsername());

        GetDataTypesCallable callable = new GetDataTypesCallable(
                getSessionUser(),
                secured != null && secured,
                used != null && used,
                readable != null && readable
        );

        try {
            List<DataTypeInfo> dataTypeInfos = callable.call();

            // Convert to response format
            List<Map<String, Object>> dataTypes = new ArrayList<>();
            for (DataTypeInfo info : dataTypeInfos) {
                Map<String, Object> dataType = new HashMap<>();
                dataType.put("elementName", info.getElementName());
                dataType.put("singular", info.getSingular());
                dataType.put("plural", info.getPlural());
                dataType.put("secured", info.isSecured());
                dataType.put("count", info.getCount());
                dataTypes.add(dataType);
            }

            return ResponseEntity.ok(dataTypes);
        } catch (Exception e) {
            log.error("Error retrieving data types", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value = "Create a new data type",
                  notes = "Creates a new XNAT data type with the specified parameters. Requires administrator privileges.")
    @ApiResponses({
        @ApiResponse(code = 201, message = "Data type created successfully"),
        @ApiResponse(code = 400, message = "Invalid request parameters"),
        @ApiResponse(code = 403, message = "Insufficient permissions or extension type not allowed"),
        @ApiResponse(code = 409, message = "Data type already exists"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    @XapiRequestMapping(value = "/create", produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Admin)
    public ResponseEntity<String> createDataType(
            @ApiParam(value = "Schema prefix (optional, will auto-generate if not provided)", required = false)
            @RequestParam(required = false) String prefix,

            @ApiParam(value = "Complex type name (optional, will derive from name if not provided)", required = false)
            @RequestParam(required = false) String complexType,

            @ApiParam(value = "Backend name of the data type", required = true)
            @RequestParam String name,

            @ApiParam(value = "Singular display name", required = true)
            @RequestParam String singular,

            @ApiParam(value = "Plural display name", required = true)
            @RequestParam String plural,

            @ApiParam(value = "Base type to extend (e.g., 'subjectAssessorData', 'imageAssessorData')", required = true)
            @RequestParam("extends") String extendsValue) {

        log.debug("User {} is creating a new data type: {}", getSessionUser().getUsername(), name);

        CreateDataTypeCallable callable = new CreateDataTypeCallable(prefix, complexType, name, singular, plural, extendsValue);

        try {
            CreateDataTypeResult result = callable.call();

            switch (result.getStatus()) {
                case SUCCESS:
                    return ResponseEntity.status(HttpStatus.CREATED).body("{\"complexType\":\"" + result.getMessage() + "\"}");
                case BAD_REQUEST:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + result.getMessage() + "\"}");
                case FORBIDDEN:
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"error\":\"" + result.getMessage() + "\"}");
                case CONFLICT:
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"error\":\"" + result.getMessage() + "\"}");
                case INTERNAL_ERROR:
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\":\"" + result.getMessage() + "\"}");
                default:
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\":\"Unknown error\"}");
            }
        } catch (Exception e) {
            log.error("Error creating data type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @ApiOperation(value = "Get a specific data type",
                  notes = "Returns detailed information about a specific data type including all its properties.")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Successfully retrieved data type"),
        @ApiResponse(code = 404, message = "Data type not found"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    @XapiRequestMapping(value = "/{elementName}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authenticated)
    public ResponseEntity<Map<String, Object>> getDataType(
            @ApiParam(value = "Element name of the data type", required = true)
            @PathVariable String elementName) {

        log.debug("User {} is requesting data type: {}", getSessionUser().getUsername(), elementName);

        try {
            Map<String, ElementSecurity> allES = ElementSecurity.GetElementSecurities();
            ElementSecurity es = allES.get(elementName);

            if (es == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Map<String, Object> dataType = new HashMap<>();
            dataType.put("elementName", es.getElementName());
            dataType.put("singular", es.getSingular());
            dataType.put("plural", es.getPlural());
            dataType.put("code", es.getCode());
            dataType.put("category", es.getCategory());
            dataType.put("usage", es.getUsage());
            dataType.put("accessible", es.getAccessible());
            dataType.put("secure", es.isSecure());
            dataType.put("secureRead", es.isSecureRead());
            dataType.put("secureEdit", es.isSecureEdit());
            dataType.put("secureCreate", es.isSecureCreate());
            dataType.put("secureDelete", es.isSecureDelete());
            dataType.put("browse", es.isBrowseable());
            dataType.put("searchable", es.isSearchable());

            return ResponseEntity.ok(dataType);
        } catch (Exception e) {
            log.error("Error retrieving data type: " + elementName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value = "Get actions for a data type",
                  notes = "Returns the list of actions configured for a specific data type.")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Successfully retrieved actions"),
        @ApiResponse(code = 404, message = "Data type not found"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    @XapiRequestMapping(value = "/{elementName}/actions", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authenticated)
    public ResponseEntity<List<Map<String, Object>>> getDataTypeActions(
            @ApiParam(value = "Element name of the data type", required = true)
            @PathVariable String elementName) {

        log.debug("User {} is requesting actions for data type: {}", getSessionUser().getUsername(), elementName);

        try {
            Map<String, ElementSecurity> allES = ElementSecurity.GetElementSecurities();
            ElementSecurity es = allES.get(elementName);

            if (es == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            List<Map<String, Object>> actions = es.getElementActions().stream()
                    .map(action -> {
                        Map<String, Object> actionMap = new HashMap<>();
                        try {
                            actionMap.put("name", action.getName());
                            actionMap.put("displayName", action.getDisplayName());
                            actionMap.put("image", action.getImage());
                            actionMap.put("popup", action.getPopup());
                            actionMap.put("popupWidth", action.getPopupWidth());
                            actionMap.put("popupHeight", action.getPopupHeight());
                            actionMap.put("secureFeature", action.getSecureFeature());
                            actionMap.put("secureAccess", action.getSecureAccess());
                            actionMap.put("parameterString", action.getParameterString());
                            actionMap.put("grouping", action.getGrouping());
                        } catch (Exception e) {
                            log.error("Error processing action", e);
                        }
                        return actionMap;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(actions);
        } catch (Exception e) {
            log.error("Error retrieving actions for data type: " + elementName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value = "Add actions to a data type",
                  notes = "Adds one or more actions to a data type. Requires administrator privileges.")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Successfully added actions"),
        @ApiResponse(code = 404, message = "Data type not found"),
        @ApiResponse(code = 400, message = "Invalid request"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    @XapiRequestMapping(value = "/{elementName}/actions", produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Admin)
    public ResponseEntity<String> addDataTypeActions(
            @ApiParam(value = "Element name of the data type", required = true)
            @PathVariable String elementName,

            @ApiParam(value = "List of actions to add", required = true)
            @RequestBody List<Map<String, String>> actionsToAdd) {

        log.debug("User {} is adding actions to data type: {}", getSessionUser().getUsername(), elementName);

        try {
            Map<String, ElementSecurity> allES = ElementSecurity.GetElementSecurities();
            ElementSecurity es = allES.get(elementName);

            if (es == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Data type not found\"}");
            }

            if(actionsToAdd.size()==0){
                return ResponseEntity.ok("{\"message\":\"No actions to add\"}");
            }

            UserI user = getSessionUser();

            // Pattern to validate action property values
            String validPattern = "^[A-Za-z0-9_:.\\-/ ]+$";

            for (Map<String, String> actionData : actionsToAdd) {
                String name = actionData.get("name");
                if (name == null || name.trim().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"Action name is required\"}");
                }
                if (!name.matches(validPattern)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"Action name can only contain letters, numbers, underscores, dots, hyphens, slashes, and spaces\"}");
                }

                name+=".."+es.getElementActions().size();

                ElementAction ea = new ElementAction(XFTItem.NewItem("xdat:element_action_type", user));
                ea.setProperty("element_action_name",name);

                if (actionData.containsKey("displayName")) {
                    String displayName = actionData.get("displayName");
                    if (!displayName.matches(validPattern)) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"Display name can only contain letters, numbers, underscores, dots, hyphens, slashes, and spaces\"}");
                    }
                    ea.setProperty("display_name",displayName);
                }
                if (actionData.containsKey("image")) {
                    String image = actionData.get("image");
                    if (!image.matches(validPattern)) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"Image can only contain letters, numbers, underscores, dots, hyphens, slashes, and spaces\"}");
                    }
                    ea.setProperty("image",image);
                }
                if (actionData.containsKey("popup")) {
                    String popup = actionData.get("popup");
                    if (!popup.matches(validPattern)) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"Popup can only contain letters, numbers, underscores, dots, hyphens, slashes, and spaces\"}");
                    }
                    ea.setProperty("popup",popup);
                }
                if (actionData.containsKey("secureFeature")) {
                    String secureFeature = actionData.get("secureFeature");
                    if (!secureFeature.matches(validPattern)) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"Secure feature can only contain letters, numbers, underscores, dots, hyphens, slashes, and spaces\"}");
                    }
                    ea.setProperty("secureFeature",secureFeature);
                }
                if (actionData.containsKey("secureAccess")) {
                    String secureAccess = actionData.get("secureAccess");
                    if (!secureAccess.matches(validPattern)) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"Secure access can only contain letters, numbers, underscores, dots, hyphens, slashes, and spaces\"}");
                    }
                    ea.setProperty("secureAccess",secureAccess);
                }
                if (actionData.containsKey("grouping")) {
                    String grouping = actionData.get("grouping");
                    if (!grouping.matches(validPattern)) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"Grouping can only contain letters, numbers, underscores, dots, hyphens, slashes, and spaces\"}");
                    }
                    ea.setProperty("grouping",grouping);
                }
                if (actionData.containsKey("parameterString")) {
                    String parameterString = actionData.get("parameterString");
                    if (!parameterString.matches(validPattern)) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"parameterString can only contain letters, numbers, underscores, dots, hyphens, slashes, and spaces\"}");
                    }
                    ea.setProperty("parameterString",parameterString);
                }


                ea.setProperty("sequence",es.getElementActions().size()+1);

                es.getItem().setChild("xdat:element_security/element_actions/element_action",ea.getItem(),false);
                es.addElementAction(ea);
            }

            // Save the updated element security
            es.save(user, true, false, null);

            return ResponseEntity.ok("{\"message\":\"Actions added successfully\"}");
        } catch (Exception e) {
            log.error("Error adding actions to data type: " + elementName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
