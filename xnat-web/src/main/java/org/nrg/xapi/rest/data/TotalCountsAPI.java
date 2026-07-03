package org.nrg.xapi.rest.data;

import static org.nrg.xdat.security.helpers.AccessLevel.Authorizer;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.authorization.GuestUserAccessXapiAuthorization;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.AuthDelegate;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Tag(name = "Total Counts API")
@XapiRestController
@RequestMapping(value = "/totalCounts")
@Slf4j
public class TotalCountsAPI extends AbstractXapiRestController {

  @Autowired
  public TotalCountsAPI(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final GroupsAndPermissionsCache cache) {
    super(userManagementService, roleHolder);
    _cache = cache;
  }

  @Operation(summary = "Gets an updated count of the data elements on the system.")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "The current counts for the system."),
      @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
      @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
  @XapiRequestMapping(value = "reset", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
  @AuthDelegate(GuestUserAccessXapiAuthorization.class)
  @ResponseBody
  public Map<String, Long> getUpdatedTotalCounts() {
    _cache.resetTotalCounts();

    final Map<String,Long> homogenizedCounts = new HashMap<>();
    homogenizedCounts.put(XnatProjectdata.SCHEMA_ELEMENT_NAME, Optional.ofNullable(XDAT.getTotalCounts().get(XnatProjectdata.SCHEMA_ELEMENT_NAME)).orElse(0L));
    homogenizedCounts.put(XnatSubjectdata.SCHEMA_ELEMENT_NAME, Optional.ofNullable(XDAT.getTotalCounts().get(XnatSubjectdata.SCHEMA_ELEMENT_NAME)).orElse(0L));
    homogenizedCounts.put(XnatImagesessiondata.SCHEMA_ELEMENT_NAME, Optional.ofNullable(XDAT.getTotalCounts().get(XnatImagesessiondata.SCHEMA_ELEMENT_NAME)).orElse(0L));

    return homogenizedCounts;
  }


  private final GroupsAndPermissionsCache _cache;
}
