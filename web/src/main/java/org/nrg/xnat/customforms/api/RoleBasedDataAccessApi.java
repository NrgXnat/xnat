package org.nrg.xnat.customforms.api;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.model.users.ElementDisplayModel;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.AuthDelegate;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.customforms.security.CustomFormUserXapiAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.nrg.xdat.security.helpers.AccessLevel.Authorizer;

@XapiRestController
@RequestMapping(value = "/role")
@Api("Role Based Data Access API")
@Slf4j
public class RoleBasedDataAccessApi extends AbstractXapiRestController {

    final XnatUserProvider userProvider;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String QUERY_PROJECTS = "SELECT proj.ID, proj.name, proj.description, proj.secondary_id, inv.firstname || ' ' || inv.lastname as investigator FROM xnat_projectData proj LEFT JOIN xnat_investigatordata inv ON proj.pi_xnat_investigatordata_id=inv.xnat_investigatordata_id WHERE PROJ.id IN (:projectIds)";


    @Autowired
    public RoleBasedDataAccessApi(final UserManagementServiceI userManagementService,
    final RoleHolder roleHolder,
    final XnatUserProvider userProvider,
    final NamedParameterJdbcTemplate jdbcTemplate) {
        super(userManagementService, roleHolder);
        this.userProvider  = userProvider;
        this.jdbcTemplate = jdbcTemplate;
    }

    @ApiOperation(value = "Gets XNAT Elements that can be created by a Form Data Manager",
            notes = "Gets XNAT Elements that can be created by a Form Data Manager",
            response = String.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/displays/createable",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            method = RequestMethod.GET)
    public ResponseEntity<List<ElementDisplayModel>> getCreatableElements() throws UserInitException, UserNotFoundException {
        final String adminUserLogin = userProvider.getLogin();
        final UserI adminUser = Users.getUser(adminUserLogin);
        return ResponseEntity.ok(getCreatableElementDisplay(adminUser));
    }

    @ApiOperation(value = "Gets XNAT Elements that can be created by a Form Data Manager",
            notes = "Gets XNAT Elements that can be created by a Form Data Manager",
            response = String.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/projects",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            method = RequestMethod.GET, restrictTo = Authorizer)
    @AuthDelegate(CustomFormUserXapiAuthorization.class)
    public ResponseEntity<List<Map<String, Object>>> getSiteProjects() throws UserInitException, UserNotFoundException{
        final String query = "SELECT proj.ID, proj.name, proj.description,proj.secondary_id, inv.firstname || ' ' || inv.lastname as investigator FROM xnat_projectData proj LEFT JOIN xnat_investigatordata inv ON proj.pi_xnat_investigatordata_id=inv.xnat_investigatordata_id;";
        List<Map<String, Object>> resultSet = jdbcTemplate.queryForList(query, EmptySqlParameterSource.INSTANCE);
        return ResponseEntity.ok(resultSet);
    }

    @ApiOperation(value = "Get specific XNAT project data that can be used by a Form Data Manager",
            response = String.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/projectsById",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.TEXT_PLAIN_VALUE,
            method = RequestMethod.POST, restrictTo = Authorizer)
    @AuthDelegate(CustomFormUserXapiAuthorization.class)
    public List<Map<String, Object>> getSelectedProjects(final @RequestBody String listOfProjects) throws UserInitException, UserNotFoundException {
        return jdbcTemplate.queryForList(QUERY_PROJECTS,
                new MapSqlParameterSource("projectIds",
                        Arrays.asList(listOfProjects.split("\\s*,\\s*"))));
    }



    private List<ElementDisplayModel>  getCreatableElementDisplay(UserI user) {
        final UserHelperServiceI helper = UserHelper.getUserHelperService(user);
        final List<ElementDisplay> displays = helper.getCreateableElementDisplays();
        return Lists.newArrayList(Iterables.filter(Lists.transform(displays, new Function<ElementDisplay, ElementDisplayModel>() {
            @Nullable
            @Override
            public ElementDisplayModel apply(@Nullable final ElementDisplay elementDisplay) {
                try {
                    return elementDisplay != null ? new ElementDisplayModel(elementDisplay) : null;
                } catch (Exception e) {
                    log.warn("An exception occurred trying to transform the element display \"{}\"", elementDisplay.getElementName(), e);
                    return null;
                }
            }
        }), Predicates.<ElementDisplayModel>notNull()));
    }

}
