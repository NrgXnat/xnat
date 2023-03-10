package org.nrg.xnat.customforms.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Api("Custom Forms Display Field API")
@Slf4j
public class CustomFormDisplayFieldApi extends AbstractXapiRestController {

    @Autowired
    public CustomFormDisplayFieldApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final FormDisplayFieldService formDisplayFieldService) {
        super(userManagementService, roleHolder);
        this.formDisplayFieldService = formDisplayFieldService;
    }

    @ApiOperation(value="Reloads Custom Form Display fields", notes = "Reloads Custom Form Display fields")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/reload", method = RequestMethod.POST)
    public void reloadCustomFormDisplayFields() {
        formDisplayFieldService.refreshDisplayFields();
    }

    private final FormDisplayFieldService formDisplayFieldService;

}
