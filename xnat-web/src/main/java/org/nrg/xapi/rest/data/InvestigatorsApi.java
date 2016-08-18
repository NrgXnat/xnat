package org.nrg.xapi.rest.data;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xdat.bean.XnatInvestigatordataBean;
import org.nrg.xdat.model.XnatInvestigatordataI;
import org.nrg.xdat.om.XnatInvestigatordata;
import org.nrg.xdat.rest.AbstractXapiRestController;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Api(description = "XNAT Data Investigators API")
@XapiRestController
@RequestMapping(value = "/investigators")
public class InvestigatorsApi extends AbstractXapiRestController {
    @Autowired
    public InvestigatorsApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final JdbcTemplate jdbcTemplate) {
        super(userManagementService, roleHolder);
        _jdbcTemplate = jdbcTemplate;
    }

    @ApiOperation(value = "Get list of investigators.", notes = "The investigators function returns a list of all investigators configured in the XNAT system.", response = XnatInvestigatordataI.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Returns a list of all of the currently configured investigators."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred")})
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<XnatInvestigatordataI>> getInvestigators() {
        return new ResponseEntity<>(_jdbcTemplate.query(PI_QUERY + PI_ORDERBY, PI_ROWMAPPER), HttpStatus.OK);
    }

    @ApiOperation(value = "Gets the requested investigator.", notes = "Returns the investigator with the specified ID.", response = XnatInvestigatordataI.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Returns the requested investigator."),
                   @ApiResponse(code = 404, message = "The requested investigator wasn't found."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred.")})
    @RequestMapping(value = "{investigatorId}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<XnatInvestigatordataI> getInvestigator(@PathVariable("investigatorId") final int investigatorId) {
        final XnatInvestigatordataI investigator = getXnatInvestigator(investigatorId);
        if (investigator == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(investigator, HttpStatus.OK);
    }

    @ApiOperation(value = "Creates a new investigator from the submitted attributes.", notes = "Returns the newly created investigator with the submitted attributes.", response = XnatInvestigatordataI.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Returns the newly created investigator."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to create the submitted investigator."),
                   @ApiResponse(code = 404, message = "The requested investigator wasn't found."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred.")})
    @RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<XnatInvestigatordataI> createInvestigator(@RequestBody final XnatInvestigatordataBean investigator) throws Exception {
        if (StringUtils.isBlank(investigator.getFirstname()) || StringUtils.isBlank(investigator.getLastname())) {
            _log.error("User {} tried to create investigator without a first or last name.", getSessionUser().getUsername());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        final UserI user = getSessionUser();
        final XFTItem item = XFTItem.NewItem(XnatInvestigatordata.SCHEMA_ELEMENT_NAME, user);
        item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".title", investigator.getTitle());
        item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".firstname", investigator.getFirstname());
        item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".lastname", investigator.getLastname());
        item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".department", investigator.getDepartment());
        item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".institution", investigator.getInstitution());
        item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".email", investigator.getEmail());
        item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".phone", investigator.getPhone());
        if (!SaveItemHelper.authorizedSave(item, user, false, false, EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.REST, EventUtils.CREATE_INVESTTGATOR))) {
            _log.error("Failed to create a new investigator for user {}. Check the logs for possible errors or exceptions.", user.getUsername());
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(getXnatInvestigator(investigator.getFirstname(), investigator.getLastname()), HttpStatus.OK);
    }

    @ApiOperation(value = "Updates the requested investigator from the submitted attributes.", notes = "Returns the updated investigator.", response = XnatInvestigatordataI.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Returns the updated investigator."),
                   @ApiResponse(code = 304, message = "The requested investigator is the same as the submitted investigator."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to edit the requested investigator."),
                   @ApiResponse(code = 404, message = "The requested investigator wasn't found."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred.")})
    @RequestMapping(value = "{investigatorId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<XnatInvestigatordataI> updateInvestigator(@PathVariable("investigatorId") final int investigatorId, @RequestBody final XnatInvestigatordataBean investigator) throws Exception {
        final UserI user = getSessionUser();
        final XnatInvestigatordata existing = XnatInvestigatordata.getXnatInvestigatordatasByXnatInvestigatordataId(investigatorId, user, false);
        if (existing == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        boolean isDirty = false;
        // Only update fields that are actually included in the submitted data and differ from the original source.
        if (StringUtils.isNotBlank(investigator.getTitle()) && !StringUtils.equals(investigator.getTitle(), existing.getTitle())) {
            existing.setTitle(investigator.getTitle());
            isDirty = true;
        }
        if (StringUtils.isNotBlank(investigator.getFirstname()) && !StringUtils.equals(investigator.getFirstname(), existing.getFirstname())) {
            existing.setFirstname(investigator.getFirstname());
            isDirty = true;
        }
        if (StringUtils.isNotBlank(investigator.getLastname()) && !StringUtils.equals(investigator.getLastname(), existing.getLastname())) {
            existing.setLastname(investigator.getLastname());
            isDirty = true;
        }
        if (StringUtils.isNotBlank(investigator.getDepartment()) && !StringUtils.equals(investigator.getDepartment(), existing.getDepartment())) {
            existing.setDepartment(investigator.getDepartment());
            isDirty = true;
        }
        if (StringUtils.isNotBlank(investigator.getInstitution()) && !StringUtils.equals(investigator.getInstitution(), existing.getInstitution())) {
            existing.setInstitution(investigator.getInstitution());
            isDirty = true;
        }
        if (StringUtils.isNotBlank(investigator.getEmail()) && !StringUtils.equals(investigator.getEmail(), existing.getEmail())) {
            existing.setEmail(investigator.getEmail());
            isDirty = true;
        }
        if (StringUtils.isNotBlank(investigator.getPhone()) && !StringUtils.equals(investigator.getPhone(), existing.getPhone())) {
            existing.setPhone(investigator.getPhone());
            isDirty = true;
        }

        if (isDirty) {
            if (!SaveItemHelper.authorizedSave(existing, user, false, false, EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.REST, EventUtils.MODIFY_INVESTTGATOR))) {
                _log.error("Failed to save the investigator with ID {}. Check the logs for possible errors or exceptions.");
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
            return new ResponseEntity<>(getXnatInvestigator(investigatorId), HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    }

    @ApiOperation(value = "Deletes the requested investigator.", notes = "Returns true if the requested investigator was successfully deleted. Returns false otherwise.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Returns true to indicate the requested investigator was successfully deleted."),
                   @ApiResponse(code = 404, message = "The requested investigator wasn't found."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred.")})
    @RequestMapping(value = "{investigatorId}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Boolean> deleteInvestigator(@PathVariable("investigatorId") final int investigatorId) throws Exception {
        final UserI user = getSessionUser();
        final XnatInvestigatordata investigator = XnatInvestigatordata.getXnatInvestigatordatasByXnatInvestigatordataId(investigatorId, user, false);
        if (investigator == null) {
            return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
        }
        SaveItemHelper.authorizedDelete(investigator.getItem(), user, EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.REST, EventUtils.REMOVE_INVESTTGATOR));
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    private XnatInvestigatordataI getXnatInvestigator(final int investigatorId) {
        final List<XnatInvestigatordataI> investigators = _jdbcTemplate.query(PI_QUERY + PI_BY_ID, new Object[]{investigatorId}, PI_ROWMAPPER);
        if (investigators.size() == 0) {
            return null;
        }
        return investigators.get(0);
    }

    private XnatInvestigatordataI getXnatInvestigator(final String firstname, final String lastname) {
        final List<XnatInvestigatordataI> investigators = _jdbcTemplate.query(PI_QUERY + PI_BY_NAME, new Object[]{firstname, lastname}, PI_ROWMAPPER);
        if (investigators.size() == 0) {
            return null;
        }
        return investigators.get(0);
    }

    private static final Logger _log = LoggerFactory.getLogger(InvestigatorsApi.class);

    // TODO: InvestigatorListResource uses a query that adds the PI's login. XnatInvestigatordataBean has no setter for login, so leaving it off for now.
    private static final String                           PI_QUERY     = "SELECT DISTINCT ON (lastname, firstname) xnat_investigatorData_id, title, firstname, lastname, institution, department, email, phone FROM xnat_investigatorData ";
    private static final String                           PI_ORDERBY   = "ORDER BY lastname, firstname";
    private static final String                           PI_BY_ID     = "WHERE xnat_investigatordata_id = ?";
    private static final String                           PI_BY_NAME   = "WHERE firstname = ? AND lastname = ?";
    private final static RowMapper<XnatInvestigatordataI> PI_ROWMAPPER = new RowMapper<XnatInvestigatordataI>() {
        @Override
        public XnatInvestigatordataI mapRow(final ResultSet resultSet, final int i) throws SQLException {
            return new XnatInvestigatordataBean() {{
                setXnatInvestigatordataId(resultSet.getInt("xnat_investigatordata_id"));
                setTitle(resultSet.getString("title"));
                setFirstname(resultSet.getString("firstname"));
                setLastname(resultSet.getString("lastname"));
                setDepartment(resultSet.getString("department"));
                setInstitution(resultSet.getString("institution"));
                setEmail(resultSet.getString("email"));
                setPhone(resultSet.getString("phone"));
            }};
        }
    };

    private final JdbcTemplate _jdbcTemplate;
}
