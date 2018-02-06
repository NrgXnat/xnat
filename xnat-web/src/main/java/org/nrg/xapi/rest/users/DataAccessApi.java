package org.nrg.xapi.rest.users;

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
import org.nrg.xapi.authorization.GuestUserAccessXapiAuthorization;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xapi.model.users.ElementDisplayModel;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.AuthDelegate;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static org.nrg.xdat.security.helpers.AccessLevel.Authorizer;

@Api(description = "Data Access API")
@XapiRestController
@RequestMapping(value = "/access")
@Slf4j
public class DataAccessApi extends AbstractXapiRestController {
    @Autowired
    public DataAccessApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
    }

    @ApiOperation(value = "Gets a list of the available element displays.", notes = "The available element displays can be used as parameters for this call in the form /xapi/access/displays/{DISPLAY}. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of available element displays."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of available element displays."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "displays", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<List<String>> getAvailableElementDisplays() {
        return new ResponseEntity<>(AVAILABLE_ELEMENT_DISPLAYS, HttpStatus.OK);
    }

    @ApiOperation(value = "Gets a list of the element displays of the specified type for the current user.", notes = "This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of element displays of the specified type for the current user."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of available element displays."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "displays/{display}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<List<ElementDisplayModel>> getElementDisplays(@PathVariable final String display) throws DataFormatException, InitializationException {
        final UserI              user   = getSessionUser();
        final UserHelperServiceI helper = UserHelper.getUserHelperService(user);
        if (helper == null) {
            throw new InitializationException("Unable to create a user helper for the user " + user.getUsername());
        }

        final List<ElementDisplay> displays;
        switch (display) {
            case "browseable":
                displays = helper.getBrowseableElementDisplays();
                break;

            case "browseableCreateable":
                displays = helper.getBrowseableCreateableElementDisplays();
                break;

            case "createable":
                displays = helper.getCreateableElementDisplays();
                break;

            case "searchable":
                displays = helper.getSearchableElementDisplays();
                break;

            case "searchableByDesc":
                displays = helper.getSearchableElementDisplaysByDesc();
                break;

            case "searchableByPluralDesc":
                displays = helper.getSearchableElementDisplaysByPluralDesc();
                break;

            default:
                throw new DataFormatException("The requested element display \"" + display + "\" is not recognized.");
        }

        final List<ElementDisplayModel> models = Lists.newArrayList(Iterables.filter(Lists.transform(displays, new Function<ElementDisplay, ElementDisplayModel>() {
            @Nullable
            @Override
            public ElementDisplayModel apply(@Nullable final ElementDisplay elementDisplay) {
                try {
                    return elementDisplay != null ? new ElementDisplayModel(elementDisplay) : null;
                } catch (Exception e) {
                    log.warn("An exception occurred trying to transform the element display \"" + elementDisplay.getElementName() + "\"", e);
                    return null;
                }
            }
        }), Predicates.<ElementDisplayModel>notNull()));

        return new ResponseEntity<>(models, HttpStatus.OK);
    }

    private static final List<String> AVAILABLE_ELEMENT_DISPLAYS = Arrays.asList("browseable", "browseableCreateable", "createable", "searchable", "searchableByDesc", "searchableByPluralDesc");
}
