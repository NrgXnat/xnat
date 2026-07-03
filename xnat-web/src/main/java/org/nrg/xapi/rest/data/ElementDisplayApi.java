/*
 * web: org.nrg.xapi.rest.data.ElementDisplayApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
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
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.display.transport.entities.ElementDisplayDB;
import org.nrg.xdat.display.transport.services.ElementDisplayStorageService;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * XNAT Element Display API
 * Provides REST endpoints for listing, retrieving, and deleting database-backed element display configurations.
 */
@Tag(name = "XNAT Element Display API")
@XapiRestController
@RequestMapping(value = "/elementdisplays")
@Slf4j
public class ElementDisplayApi extends AbstractXapiRestController {

    @Autowired
    public ElementDisplayApi(final UserManagementServiceI userManagementService,
                             final RoleHolder roleHolder,
                             final ElementDisplayStorageService elementDisplayStorageService) {
        super(userManagementService, roleHolder);
        _elementDisplayStorageService = elementDisplayStorageService;
    }

    @Operation(summary = "Get all element displays", description = "Returns a list of all database-backed element display configurations.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved element displays"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @XapiRequestMapping(value = "", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public ResponseEntity<List<ElementDisplayDB>> getAllElementDisplays() {
        log.debug("User {} is requesting all element displays", getSessionUser().getUsername());
        try {
            return ResponseEntity.ok(_elementDisplayStorageService.getAll());
        } catch (Exception e) {
            log.error("Error retrieving element displays", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get an element display by element name", description = "Returns the database-backed element display configuration for the specified element name (xsi:type).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved element display"),
        @ApiResponse(responseCode = "404", description = "Element display not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @XapiRequestMapping(value = "/{elementName}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public ResponseEntity<ElementDisplayDB> getElementDisplay(
            @Parameter(description = "The element name (xsi:type) of the element display", required = true)
            @PathVariable String elementName) {
        log.debug("User {} is requesting element display for: {}", getSessionUser().getUsername(), elementName);
        try {
            final ElementDisplayDB elementDisplay = _elementDisplayStorageService.findByElementName(elementName);
            if (elementDisplay == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(elementDisplay);
        } catch (Exception e) {
            log.error("Error retrieving element display for: " + elementName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Delete an element display by element name", description = "Deletes the database-backed element display configuration for the specified element name and removes it from the in-memory DisplayManager.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Element display deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Element display not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @XapiRequestMapping(value = "/{elementName}", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = Admin)
    public ResponseEntity<String> deleteElementDisplay(
            @Parameter(description = "The element name (xsi:type) of the element display to delete", required = true)
            @PathVariable String elementName) {
        log.debug("User {} is requesting to delete element display for: {}", getSessionUser().getUsername(), elementName);
        try {
            final ElementDisplayDB elementDisplay = _elementDisplayStorageService.findByElementName(elementName);
            if (elementDisplay == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Element display not found for: " + elementName + "\"}");
            }

            _elementDisplayStorageService.delete(elementDisplay);

            log.info("Successfully deleted element display for: {}", elementName);
            return ResponseEntity.ok("{\"message\":\"Element display deleted successfully\"}");
        } catch (Exception e) {
            log.error("Error deleting element display for: " + elementName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Failed to delete element display: " + e.getMessage() + "\"}");
        }
    }

    private final ElementDisplayStorageService _elementDisplayStorageService;
}
