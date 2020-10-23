package org.nrg.xapi.rest.pipeline;

import static org.nrg.xdat.security.helpers.AccessLevel.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.services.SerializerService;
import org.nrg.pipeline.*;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.Project;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.om.ArcProject;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.ResourceData;
import org.nrg.xnat.preferences.PipelinePreferences;
import org.nrg.xnat.restlet.representations.JSONTableRepresentation;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Mohana Ramaratnam
 */
@Api("XNAT Traditional Pipeline Management API")
@XapiRestController
@RequestMapping(value = "/pipelines")
@Slf4j
public class PipelineApi extends AbstractXapiRestController {
    @Autowired
    public PipelineApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final CatalogService catalogService, final JdbcTemplate jdbcTemplate, final SerializerService serializerService, final SiteConfigPreferences siteConfigPreferences, final PipelinePreferences pipelinePreferences, final AliasTokenService aliasTokenService) {
        super(userManagementService, roleHolder);
        this._catalogService = catalogService;
        _jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        _serializerService = serializerService;
        _siteConfigPreferences = siteConfigPreferences;
        _pipelinePreferences = pipelinePreferences;
        _aliasTokenService = aliasTokenService;
        _isWindows = System.getProperty("os.name").toUpperCase().startsWith("WINDOWS");
    }

    @XapiRequestMapping(value = {"/site"}, method = GET, restrictTo = Authenticated, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a list of site wide pipelines for a given datatype (optional)")
    public String getSitePipelines(@RequestParam(value = "xsiType", required = false) final String xsiType) throws InitializationException {
        final ArcProject arcProject;
        try {
            arcProject = xsiType == null ? PipelineRepositoryManager.GetInstance().createNewArcProjectForDummyProject() : PipelineRepositoryManager.GetInstance().createNewArcProjectForDummyProject(xsiType);
        } catch (Exception e) {
            throw new InitializationException("Unable to generate generic arc project", e);
        }
        if (arcProject == null) {
            throw new InitializationException("Unable to generate generic arc project");
        }
        final XFTTable table = PipelineRepositoryManager.GetInstance().toTable(arcProject);
        try {
            return getJsonAsStringResponse(table);
        } catch (IOException e) {
            throw new InitializationException("Unable to generate generic arc project", e);
        }
    }

    @XapiRequestMapping(value = {"/project/{projectId}"}, method = GET, restrictTo = Authenticated, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a list of project-enabled pipelines for a given datatype (optional)")
    public String getProjectPipelines(@PathVariable(value = "projectId") final String projectId, @RequestParam(value = "xsiType", required = false) final String xsiType) throws NotFoundException, InitializationException {
        final XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(projectId, getSessionUser(), false);
        if (project == null) {
            throw new NotFoundException(XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId);
        }
        final ArcProject arcProject = project.getArcSpecification();
        if (arcProject == null) {
            throw new InitializationException("Invalid configuration for project " + projectId);
        }
        final XFTTable table = PipelineRepositoryManager.GetInstance().toTable(arcProject);
        if (StringUtils.isNotBlank(xsiType)) {
            // filter for only pipelines that apply to the datatype
            final ArrayList<String> cols = new ArrayList<>();
            Collections.addAll(cols, table.getColumns());
            final ArrayList<Object[]> rows = new ArrayList<>();
            while (table.hasMoreRows()) {
                final Object[] row              = table.nextRow();
                final String   pipelineDataType = (String) row[5];
                if (StringUtils.equalsAny(pipelineDataType, ALL_DATATYPES, xsiType)) {
                    rows.add(row);
                }
            }
            table.initTable(cols, rows);
        }
        try {
            return getJsonAsStringResponse(table);
        } catch (Exception e) {
            throw new InitializationException("An error occurred trying to return the pipelines for the project " + projectId, e);
        }
    }

    @XapiRequestMapping(value = "/parameters", method = GET, restrictTo = Authenticated, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get the site-wide parameter details for the pipeline identified by its name; optionally pass the project id to get the project specific parameters")
    public String getSitePipelineParameters(@RequestParam("pipelinename") final String pipelineName, @RequestParam(value = "project", required = false) final String projectId) throws InitializationException {
        final PipelineDetailsHelper pipelineDetailsHelper = new PipelineDetailsHelper(projectId);
        try {
            final Map<String, Object> pipelineDetails = pipelineDetailsHelper.getPipelineDetailsMap(pipelineName, true);
            // Make a json object from the pipelineDetails map
            return _serializerService.toJson(pipelineDetails);
        } catch (Exception e) {
            final String message = StringUtils.isNotBlank(projectId)
                                   ? "An error occurred trying to retrieve the parameters for the pipeline " + pipelineName + " for the project " + projectId
                                   : "An error occurred trying to retrieve the site-wide parameters for the pipeline " + pipelineName;
            throw new InitializationException(message, e);
        }
    }

    @XapiRequestMapping(value = {"/launch/{pipelineNameOrStep}"}, method = POST, restrictTo = Edit, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Resolve the parameters and launch the pipeline")
    public PipelineLaunchReport launchPipelineWQueryParams(@RequestParam(value = "project", required = false) final String projectId,
                                                           @PathVariable("pipelineNameOrStep") final String pipelineNameOrStep,
                                                           final @RequestBody Map<String, String> allRequestParams) throws DataFormatException, NotFoundException {
        if (StringUtils.isNotBlank(projectId) && !Permissions.verifyProjectExists(_jdbcTemplate, projectId)) {
            throw new NotFoundException(XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId);
        }

        //This JSON may look like
        //{"Experiments":"[\"/archive/experiments/XNAT_E00001\",\"/archive/experiments/XNAT_E00003\"]","create_nii":"Y","overwrite":"Y"}
        final String experimentIdOrUri = getExperiments(allRequestParams);
        if (StringUtils.isBlank(experimentIdOrUri)) {
            throw new DataFormatException(NO_EXPERIMENTS_FOUND);
        }

        log.info("Launch requested for pipeline {} on experiment {}", pipelineNameOrStep, experimentIdOrUri);

        final UserI                      user             = getSessionUser();
        final List<PipelineLaunchStatus> pipelineLaunch   = new ArrayList<>();
        final AtomicInteger              successCount     = new AtomicInteger();
        final AtomicInteger              failureCount     = new AtomicInteger();
        final Map<String, String>        queryParams      = extractJustParameters(allRequestParams);
        final Map<String, String>        schemaLinkParams = extractSchemaLinkParameters(allRequestParams);

        for (final String expIdOrUri : getExperimentsFromIdOrUri(experimentIdOrUri)) {
            try {
                final String             experimentId = getExperimentIdFromUri(expIdOrUri);
                final XnatExperimentdata experiment   = XnatExperimentdata.getXnatExperimentdatasById(experimentId, user, false);
                if (experiment == null) {
                    pipelineLaunch.add(markLaunchFailure(projectId, experimentId, ""));
                    failureCount.incrementAndGet();
                    break;
                }
                final String _projectId = StringUtils.defaultIfBlank(projectId, experiment.getProject());
                if (!Permissions.verifyProjectExists(_jdbcTemplate, _projectId)) {
                    pipelineLaunch.add(markLaunchFailure(_projectId, experimentId, ""));
                    failureCount.incrementAndGet();
                    break;
                }

                final XnatProjectdata     project           = XnatProjectdata.getProjectByIDorAlias(_projectId, user, false);
                final Map<String, String> xmlDocumentParams = new HashMap<>();
                final Map<String, String> bodyParams        = new HashMap<>();
                final Map<String, String> qParams           = new HashMap<>(queryParams);
                qParams.putAll(resolveSchemaLink(experiment, schemaLinkParams));

                final PipelineLaunchHandler pipelineLaunchHandler = new PipelineLaunchHandler(project, experiment, pipelineNameOrStep);
                final boolean               status                = pipelineLaunchHandler.handleLaunch(bodyParams, qParams, xmlDocumentParams, "", user);
                if (status) {
                    successCount.incrementAndGet();
                    pipelineLaunch.add(markLaunchSuccess(projectId, experimentId, experiment.getLabel()));
                } else {
                    failureCount.incrementAndGet();
                    pipelineLaunch.add(markLaunchFailure(projectId, experimentId, experiment.getLabel()));
                }
            } catch (Exception ce) {
                failureCount.incrementAndGet();
                pipelineLaunch.add(markLaunchFailure(projectId, expIdOrUri, ""));
            }

        }

        final PipelineLaunchReport launchReport = new PipelineLaunchReport();
        launchReport.setSuccesses(successCount.get());
        launchReport.setFailures(failureCount.get());
        launchReport.setExperimentLaunchStatuses(pipelineLaunch);
        launchReport.setParams(queryParams);
        return launchReport;
    }

    @ApiOperation(value = "Resolve the parameters and terminate the pipeline")
    @ApiResponses({@ApiResponse(code = 200, message = "Parameters resolved and pipeline successfully terminated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to terminate the specified pipeline."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = {"/terminate/{pipelineNameOrStep}/project/{projectId}"}, method = POST, restrictTo = Edit,
                        produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PipelineLaunchReport terminate(@PathVariable("pipelineNameOrStep") final String pipelineNameOrStep,
                                          @PathVariable("projectId") @Project final String projectId,
                                          final @RequestBody Map<String, String> allRequestParams) throws DataFormatException {
        log.debug("Terminating pipeline name or step {} for project {} with the following parameters: {}", pipelineNameOrStep, projectId, allRequestParams);

        //This JSON may look like
        //{"experiments":"[\"/archive/experiments/XNAT_E00001\",\"/archive/experiments/XNAT_E00003\"]", "pipelinePath":"/data/pipeline/catalog/dicom/DicomToNifti.xml"}
        final String experimentIdOrUri = getExperiments(allRequestParams);
        final String pipelinePath      = allRequestParams.get(PIPELINE_PATH);
        if (StringUtils.isBlank(experimentIdOrUri)) {
            throw new DataFormatException(NO_EXPERIMENTS_FOUND);
        }
        if (StringUtils.isBlank(pipelinePath)) {
            throw new DataFormatException(NO_PIPELINE_PATH_FOUND);
        }

        final UserI                      user           = getSessionUser();
        final AtomicInteger              successCount   = new AtomicInteger();
        final AtomicInteger              failureCount   = new AtomicInteger();
        final List<PipelineLaunchStatus> pipelineLaunch = new ArrayList<>();
        final Map<String, String>        idToLabel      = new HashMap<>();
        final List<String>               experimentIds  = new ArrayList<>();
        for (final String expIdOrUri : getExperimentsFromIdOrUri(experimentIdOrUri)) {
            try {
                final String             experimentId = getExperimentIdFromUri(expIdOrUri);
                final XnatExperimentdata experiment   = XnatExperimentdata.getXnatExperimentdatasById(experimentId, user, false);
                if (experiment == null) {
                    pipelineLaunch.add(markLaunchFailure(projectId, experimentId, ""));
                    failureCount.incrementAndGet();
                    break;
                }
                experimentIds.add(experimentId);
                idToLabel.put(experimentId, experiment.getLabel());
            } catch (ClientException ce) {
                failureCount.incrementAndGet();
                pipelineLaunch.add(markLaunchFailure(projectId, expIdOrUri, ""));
            }
        }

        final List<Map<String, Object>> dbWorkflowRows = getLatestWorkflow(pipelinePath, projectId, experimentIds);
        for (final Map<String, Object> row : dbWorkflowRows) {
            final Integer wId          = (Integer) row.get("wrk_workflowdata_id");
            final String  jId          = (String) row.get("jobId");
            final String  experimentId = (String) row.get("id");
            final String  label        = idToLabel.get(experimentId);
            if (triggerTerminate(user, experimentId, wId.toString(), jId, label, projectId, pipelinePath)) {
                successCount.incrementAndGet();
                pipelineLaunch.add(markLaunchSuccess(projectId, experimentId, ""));
            } else {
                failureCount.incrementAndGet();
                pipelineLaunch.add(markLaunchFailure(projectId, experimentId, ""));
            }
        }

        idToLabel.put("pipelinePath", pipelinePath);

        final PipelineLaunchReport launchReport = new PipelineLaunchReport();
        launchReport.setSuccesses(successCount.get());
        launchReport.setFailures(failureCount.get());
        launchReport.setExperimentLaunchStatuses(pipelineLaunch);
        launchReport.setParams(idToLabel);
        return launchReport;
    }

    @ApiOperation(value = "Indicates whether the AutoRun pipeline is enabled or disabled on a site-wide or per-project basis")
    @ApiResponses({@ApiResponse(code = 200, message = "AutoRun configuration for site or project successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to check AutoRun settings for the site or specified project."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = {"/autoRun", "/autoRun/projects/{projectId}"}, method = GET, restrictTo = Read, produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean isAutoRunEnabled(@PathVariable(value = "projectId", required = false) @Project final String projectId) throws NotFoundException {
        final boolean isProjectSpecified = StringUtils.isBlank(projectId);
        if (isProjectSpecified) {
            if (!Permissions.verifyProjectExists(_jdbcTemplate, projectId)) {
                throw new NotFoundException(XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId);
            }
            log.debug("User {} checking whether AutoRun pipeline is enabled for project {}", getSessionUser().getUsername(), projectId);
            return _pipelinePreferences.isAutoRunEnabled(projectId);
        }
        log.debug("User {} checking whether AutoRun pipeline is enabled for site", getSessionUser().getUsername());
        return _pipelinePreferences.isAutoRunEnabled();
    }

    @ApiOperation(value = "Sets whether the AutoRun pipeline is enabled or disabled on a site-wide or per-project basis")
    @ApiResponses({@ApiResponse(code = 200, message = "AutoRun successfully configured for site or project."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to modify AutoRun settings for the site or specified project."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = {"/autoRun", "/autoRun/projects/{projectId}"}, method = PUT, restrictTo = Edit, produces = MediaType.APPLICATION_JSON_VALUE)
    public void setAutoRunEnabled(@PathVariable(value = "projectId", required = false) @Project final String projectId, @RequestParam final boolean enable) throws NotFoundException {
        final boolean isProjectSpecified = StringUtils.isBlank(projectId);
        if (isProjectSpecified) {
            if (!Permissions.verifyProjectExists(_jdbcTemplate, projectId)) {
                throw new NotFoundException(XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId);
            }
            log.debug("User {} {} AutoRun pipeline for project {}", getSessionUser().getUsername(), enable ? "enabling" : "disabling", projectId);
            _pipelinePreferences.setAutoRunEnabled(projectId, enable);
        }
        log.debug("User {} {} AutoRun pipeline for site", getSessionUser().getUsername(), enable ? "enabling" : "disabling");
        _pipelinePreferences.setAutoRunEnabled(enable);
    }

    private boolean terminateFileExists(final String command) {
        final File file = new File(command);
        return file.exists() && file.canExecute();
    }

    private boolean triggerTerminate(UserI user, String xnatId, String workflowId, String jobId, String label, String projectId, String pipelinePath) {
        final String  command    = Paths.get(_siteConfigPreferences.getPipelinePath(), "bin", "killxnatpipeline").toString() + (_isWindows ? ".bat" : "");
        final boolean fileExists = terminateFileExists(command);
        if (!fileExists) {
            return false;
        }

        final String pipelineUrl = _siteConfigPreferences.getProcessingUrl();
        final String host        = StringUtils.isNotBlank(pipelineUrl) ? pipelineUrl : TurbineUtils.GetFullServerPath();


        final AliasToken    token  = _aliasTokenService.issueTokenForUser(user);
        final StringBuilder buffer = new StringBuilder(command);
        buffer.append(" -id ");
        buffer.append(xnatId);
        buffer.append(" -host ");
        buffer.append(host);
        buffer.append(" -u ");
        buffer.append(token.getAlias());
        buffer.append(" -pwd ");
        buffer.append(token.getSecret());
        buffer.append(" -workflowId ");
        buffer.append(workflowId);
        buffer.append(" -jobId ");
        buffer.append(jobId == null ? " null " : jobId);
        buffer.append(" -pipelinePath ");
        buffer.append(pipelinePath);
        buffer.append(" -project ");
        buffer.append(projectId);
        buffer.append(" -label ");
        buffer.append(label);

        final String commandWithArguments = buffer.toString().trim();
        try {
            launchLogger.info("Terminating pipeline with command: {}", commandWithArguments);
            final ProcessLauncher processLauncher = new ProcessLauncher();
            processLauncher.setCommand(commandWithArguments);
            processLauncher.start();
            return true;
        } catch (Exception e) {
            launchLogger.error("An error occurred trying to run a pipeline with command {}", buffer, e);
            return false;
        }
    }

    private Map<String, String> extractJustParameters(Map<String, String> parameters) {
        return extractParameters(parameters, key -> !StringUtils.equalsIgnoreCase(key, EXPERIMENTS) && !StringUtils.startsWith(key, SCHEMALINK_NAME_START));
    }

    private Map<String, String> extractSchemaLinkParameters(final Map<String, String> parameters) {
        return extractParameters(parameters, key -> StringUtils.startsWith(key, SCHEMALINK_NAME_START));
    }

    private Map<String, String> extractParameters(final Map<String, String> parameters, final Predicate<String> predicate) {
        return parameters.keySet().stream().filter(predicate).collect(Collectors.toMap(key -> StringUtils.strip(key, SCHEMALINK_NAME_START), parameters::get));
    }

    private Map<String, String> resolveSchemaLink(final ItemI om, final Map<String, String> schemaLinkParam) {
        return schemaLinkParam.keySet().stream().collect(Collectors.toMap(Function.identity(), key -> resolveValues(om, key)));
    }

    private static String resolveValues(final ItemI om, final String schemaLink) {
        final Object object;
        try {
            object = om.getItem().getProperty(schemaLink, true);
        } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e) {
            throw new RuntimeException("An error occurred trying to resolve the " + schemaLink + " property on object " + om, e);
        }
        if (object == null) {
            return "";
        }
        try {
            //noinspection unchecked
            final ArrayList<? extends Class<?>> matches = (ArrayList<? extends Class<?>>) object;
            if (matches.size() == 1) {
                return matches.get(0).toString();
            }
            return "[" + matches.stream().map(Object::toString).collect(Collectors.joining(",")) + "]";
        } catch (ClassCastException cce) {
            return object.toString();
        }
    }

    private PipelineLaunchStatus markLaunchSuccess(String projectId, String expId, String label) {
        PipelineLaunchStatus pStatus = new PipelineLaunchStatus();
        pStatus.setStatus("SUCCESS");
        pStatus.setProject(projectId);
        pStatus.setExptId(expId);
        pStatus.setExptLabel(label);
        return pStatus;
    }

    private PipelineLaunchStatus markLaunchFailure(String projectId, String expId, String label) {
        PipelineLaunchStatus pStatus = new PipelineLaunchStatus();
        pStatus.setStatus("FAILURE");
        pStatus.setProject(projectId);
        pStatus.setExptId(expId);
        pStatus.setExptLabel(label);
        return pStatus;
    }

    private List<Map<String, Object>> getLatestWorkflow(final String pipelineName, final String projectId, final List<String> experimentIds) {
        return _jdbcTemplate.queryForList(WORKFLOW_QUERY, new MapSqlParameterSource(PIPELINE_NAME, pipelineName).addValue(PROJECT, projectId).addValue(EXPERIMENT_IDS, experimentIds));
    }

    @Nonnull
    private String getJsonAsStringResponse(final XFTTable table) throws IOException {
        final JSONTableRepresentation jsonTableRep = new JSONTableRepresentation(table, org.restlet.data.MediaType.APPLICATION_JSON);
        try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            jsonTableRep.write(stream);
            return new String(stream.toByteArray());
        }
    }

    private String[] getExperimentsFromIdOrUri(final String experimentIdOrUri) {
        return StringUtils.split(RegExUtils.removeAll(experimentIdOrUri, "[\\[\\]]"), ",");
    }

    private String getExperimentIdFromUri(final String xnatIdOrUri) throws ClientException {
        final ResourceData resourceData = _catalogService.getResourceDataFromUri(xnatIdOrUri.replace("\"", ""));
        if (resourceData != null) {
            final ArchivableItem item = resourceData.getItem();
            if (item != null) {
                return item.getId();
            }
        }
        return null;
    }

    @Nullable
    private String getExperiments(final Map<String, String> parameters) {
        if (parameters.containsKey(EXPERIMENTS)) {
            return parameters.get(EXPERIMENTS);
        }
        if (parameters.containsKey(EXPERIMENTS_LC)) {
            return parameters.get(EXPERIMENTS_LC);
        }
        if (parameters.containsKey(EXPERIMENTS_UC)) {
            return parameters.get(EXPERIMENTS_UC);
        }
        return null;
    }

    private static final Logger launchLogger           = LoggerFactory.getLogger("org.nrg.pipeline.launch");
    private static final String ALL_DATATYPES          = "All Datatypes";
    private static final String PIPELINE_PATH          = "pipelinePath";
    private static final String EXPERIMENTS            = "Experiments";
    private static final String EXPERIMENTS_LC         = EXPERIMENTS.toLowerCase();
    private static final String EXPERIMENTS_UC         = EXPERIMENTS.toUpperCase();
    private static final String NO_EXPERIMENTS_FOUND   = "No data for experiment list found under keys \"" + EXPERIMENTS + "\", \"" + EXPERIMENTS_LC + "\", or \"" + EXPERIMENTS_UC + "\"";
    private static final String NO_PIPELINE_PATH_FOUND = "No data for pipeline path found under key \"" + PIPELINE_PATH + "\"";
    private static final String SCHEMALINK_NAME_START  = "xnatschemaLink-";
    private static final String PIPELINE_NAME          = "pipelineName";
    private static final String PROJECT                = "project";
    private static final String EXPERIMENT_IDS         = "experimentIds";
    private static final String WORKFLOW_QUERY         = "WITH " +
                                                         "    inn AS (SELECT " +
                                                         "                t.wrk_workflowdata_id, " +
                                                         "                t.jobid, " +
                                                         "                t.launch_time, " +
                                                         "                t.externalid, " +
                                                         "                t.id, " +
                                                         "                ROW_NUMBER() OVER (PARTITION BY t.id ORDER BY t.launch_time DESC) num " +
                                                         "            FROM " +
                                                         "                wrk_workflowdata t " +
                                                         "            WHERE " +
                                                         "                pipeline_name = :" + PIPELINE_NAME + " AND " +
                                                         "                externalid = :" + PROJECT + " AND " +
                                                         "                id IN (:" + EXPERIMENT_IDS + ") " +
                                                         "    ) " +
                                                         "SELECT " +
                                                         "    wrk_workflowdata_id, " +
                                                         "    jobid, " +
                                                         "    launch_time, " +
                                                         "    externalid, " +
                                                         "    id " +
                                                         "FROM " +
                                                         "    inn " +
                                                         "WHERE " +
                                                         "    num = 1";

    private final CatalogService             _catalogService;
    private final NamedParameterJdbcTemplate _jdbcTemplate;
    private final SerializerService          _serializerService;
    private final SiteConfigPreferences      _siteConfigPreferences;
    private final PipelinePreferences        _pipelinePreferences;
    private final AliasTokenService          _aliasTokenService;
    private final boolean                    _isWindows;
}
