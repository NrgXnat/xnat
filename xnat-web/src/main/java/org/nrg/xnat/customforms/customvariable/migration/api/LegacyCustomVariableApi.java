package org.nrg.xnat.customforms.customvariable.migration.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Api("Legacy Custom Variable API")
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


    @ApiOperation(value = "Get List of All Custom Variables", notes = "Gets a list of existing legacy custom variables", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Unexpected error")})
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


    @ApiOperation(value = "Migrate legacy custom variable to Dynamic Variable", notes = "Generate a forms IO JSON for pre-formsio Custom Variable Definition", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Not enabled"),
            @ApiResponse(code = 500, message = "Unexpected error")})
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
