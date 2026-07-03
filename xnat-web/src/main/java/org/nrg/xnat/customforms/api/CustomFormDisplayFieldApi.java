package org.nrg.xnat.customforms.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.customforms.service.FormDisplayFieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@XapiRestController
@RequestMapping(value = "/customforms/displayfields")
@Tag(name = "Custom Forms Display Field API")
@Slf4j
public class CustomFormDisplayFieldApi extends AbstractXapiRestController {

    @Autowired
    public CustomFormDisplayFieldApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final FormDisplayFieldService formDisplayFieldService) {
        super(userManagementService, roleHolder);
        this.formDisplayFieldService = formDisplayFieldService;
    }

    @Operation(summary = "Reloads Custom Form Display fields", description = "Reloads Custom Form Display fields")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "/reload", method = RequestMethod.POST)
    public void reloadCustomFormDisplayFields() {
        formDisplayFieldService.refreshDisplayFields();
    }

    private final FormDisplayFieldService formDisplayFieldService;

}
