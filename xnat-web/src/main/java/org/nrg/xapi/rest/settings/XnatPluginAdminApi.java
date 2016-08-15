package org.nrg.xapi.rest.settings;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xdat.rest.AbstractXapiRestController;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.services.XnatAppInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

@Api(description = "XNAT Plugin Admin API")
@XapiRestController
@RequestMapping(value = "/plugins")
public class XnatPluginAdminApi extends AbstractXapiRestController {
    @Autowired
    public XnatPluginAdminApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final XnatAppInfo appInfo) {
        super(userManagementService, roleHolder);
        _appInfo = appInfo;
    }

    @ApiOperation(value = "Returns a list of all of the installed XNAT plugins.", notes = "The maps returned from this call include all of the properties specified in the plugin's property file.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT plugin properties successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<Map<String, Properties>> getAllDataTypeSchemas() throws IOException {
        return new ResponseEntity<>(_appInfo.getPluginProperties(), HttpStatus.OK);
    }

    @ApiOperation(value = "Returns the requested XNAT plugin properties.", notes = "The maps returned from this call include all of the properties specified in the plugin's property file.", response = Properties.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT plugin properties successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 404, message = "The requested resource wasn't found."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = "{plugin}", produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<Properties> getRequestedDataTypeSchema(@PathVariable("plugin") final String plugin) throws IOException {
        final Map<String, Properties> plugins = _appInfo.getPluginProperties();
        if (!plugins.containsKey(plugin)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(plugins.get(plugin), HttpStatus.OK);
    }

    private final XnatAppInfo _appInfo;
}