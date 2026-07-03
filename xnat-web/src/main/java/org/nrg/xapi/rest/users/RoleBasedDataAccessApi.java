package org.nrg.xnat.customforms.api;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.nrg.xapi.authorization.PrivilegedUserXapiAuthorization;
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
@Tag(name = "Role Based Data Access API")
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

    @Operation(summary = "Gets XNAT Elements that can be created by a Form Data Manager", description = "Gets XNAT Elements that can be created by a Form Data Manager")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "/displays/createable",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            method = RequestMethod.GET)
    public ResponseEntity<List<ElementDisplayModel>> getCreatableElements() throws UserInitException, UserNotFoundException {
        final String adminUserLogin = userProvider.getLogin();
        final UserI adminUser = Users.getUser(adminUserLogin);
        return ResponseEntity.ok(getCreatableElementDisplay(adminUser));
    }

    @Operation(summary = "Gets XNAT Elements that can be created by a Form Data Manager", description = "Gets XNAT Elements that can be created by a Form Data Manager")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "/projects",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            method = RequestMethod.GET, restrictTo = Authorizer)
    @AuthDelegate(PrivilegedUserXapiAuthorization.class)
    public ResponseEntity<List<Map<String, Object>>> getSiteProjects() throws UserInitException, UserNotFoundException{
        final String query = "SELECT proj.ID, proj.name, proj.description,proj.secondary_id, inv.firstname || ' ' || inv.lastname as investigator FROM xnat_projectData proj LEFT JOIN xnat_investigatordata inv ON proj.pi_xnat_investigatordata_id=inv.xnat_investigatordata_id;";
        List<Map<String, Object>> resultSet = jdbcTemplate.queryForList(query, EmptySqlParameterSource.INSTANCE);
        return ResponseEntity.ok(resultSet);
    }

    @Operation(summary = "Get specific XNAT project data that can be used by a Form Data Manager")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "/projectsById",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.TEXT_PLAIN_VALUE,
            method = RequestMethod.POST, restrictTo = Authorizer)
    @AuthDelegate(PrivilegedUserXapiAuthorization.class)
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
