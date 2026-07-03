package org.nrg.xnat.customforms.customvariable.migration.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.AuthorizedRoles;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.customforms.customvariable.migration.service.CustomVariableMigrator;
import org.nrg.xnat.customforms.customvariable.migration.service.LegacyCustomVariableMigrator;
import org.nrg.xnat.features.CustomFormsFeatureFlags;
import org.nrg.xnat.customforms.pojo.CollatedLegacyCustomVariable;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static org.nrg.xdat.security.helpers.AccessLevel.Role;

@XapiRestController
@RequestMapping(value = "/legacycustomvariable")
@Tag(name = "Legacy Custom Variable API")
@Slf4j

public class LegacyCustomVariableApi extends AbstractXapiRestController {

    private final CustomVariableMigrator customVariableMigrator;
    private final LegacyCustomVariableMigrator legacyCustomVariableMigrator;
    private final CustomFormsFeatureFlags customFormsFeatureFlags;

    @Autowired
    public LegacyCustomVariableApi(final UserManagementServiceI userManagementService,
                                   final RoleHolder roleHolder,
                                   final CustomVariableMigrator customVariableMigrator,
                                   final LegacyCustomVariableMigrator legacyCustomVariableMigrator,
                                   final CustomFormsFeatureFlags customFormsFeatureFlags
    ) {
        super(userManagementService, roleHolder);
        this.customVariableMigrator = customVariableMigrator;
        this.legacyCustomVariableMigrator = legacyCustomVariableMigrator;
        this.customFormsFeatureFlags = customFormsFeatureFlags;
    }


    @Operation(summary = "Get List of All Custom Variables", description = "Gets a list of existing legacy custom variables")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public ResponseEntity<List<CollatedLegacyCustomVariable>> getAllLegacyCustomVariables() {
        try {
            final UserI user = XDAT.getUserDetails();
            boolean filter = true;
            if (Roles.isSiteAdmin(user.getUsername()) || Roles.checkRole(user, CustomFormsConstants.FORM_MANAGER_ROLE)) {
                filter = false;
            }
            return new ResponseEntity<>(customVariableMigrator.getAllFieldDefinitions(user,filter), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @Operation(summary = "Migrate legacy custom variable to Dynamic Variable", description = "Generate a forms IO JSON for pre-formsio Custom Variable Definition")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Not enabled"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "/migratetoformio/{field_definition_id}",  method = RequestMethod.POST, restrictTo = Role)
    @AuthorizedRoles({CustomFormsConstants.ADMIN_ROLE, CustomFormsConstants.FORM_MANAGER_ROLE})
    public ResponseEntity<Void> migrateCustomVariableToDynamicVariable(final @PathVariable String field_definition_id,
                                                                         final @RequestParam(required = false) String trackingId
    ) {
        if (!customFormsFeatureFlags.isCustomVariableMigrationEnabled()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            legacyCustomVariableMigrator.migrateToFormIO(field_definition_id, trackingId, getSessionUser());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
