package org.nrg.dcm.xnat.xapi;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.nrg.dcm.xnat.exceptions.InvalidEntityException;
import org.nrg.dcm.xnat.pojos.DicomMapping;
import org.nrg.dcm.xnat.services.DicomMappingService;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;

import java.util.List;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;

@Api
@XapiRestController
@RequestMapping(value = "/dicom_mappings")
@Slf4j
public class DicomMappingApi extends AbstractXapiRestController {
    private final DicomMappingService dicomMappingService;

    @Autowired
    public DicomMappingApi(final DicomMappingService dicomMappingService,
                           final UserManagementServiceI userManagementService,
                           final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
        this.dicomMappingService = dicomMappingService;
    }


    @ApiResponses({@ApiResponse(code = 200, message = "The operation completed successfully."),
            @ApiResponse(code = 400, message = "Something is wrong with your request"),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "Unexpected error.")})
    @XapiRequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, restrictTo = Admin)
    @ResponseBody
    public ResponseEntity<List<DicomMapping>> getAll() {
        try {
            return new ResponseEntity<>(dicomMappingService.getAllPojos(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Create or update.")
    @ApiResponses({@ApiResponse(code = 200, message = "The operation completed successfully."),
            @ApiResponse(code = 400, message = "Something is wrong with your request"),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "Unexpected error.")})
    @XapiRequestMapping(value = "update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_HTML_VALUE,
            method = RequestMethod.PUT, restrictTo = Admin)
    @ResponseBody
    public ResponseEntity<String> update(final @RequestBody DicomMapping pojo) {
        try {
            dicomMappingService.createOrUpdateFromPojo(pojo, getSessionUser());
        } catch (NotFoundException | InvalidEntityException | HibernateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Success!", HttpStatus.OK);
    }

    @ApiOperation(value = "Delete.")
    @ApiResponses({@ApiResponse(code = 200, message = "The operation completed successfully."),
            @ApiResponse(code = 400, message = "Something is wrong with your request"),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "Unexpected error.")})
    @XapiRequestMapping(value = "{id}", produces = MediaType.TEXT_HTML_VALUE, method = RequestMethod.DELETE,
            restrictTo = Admin)
    @ResponseBody
    public ResponseEntity<String> delete(final @PathVariable long id) {
        try {
            dicomMappingService.delete(id);
        } catch (HibernateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Success!", HttpStatus.OK);
    }
}
