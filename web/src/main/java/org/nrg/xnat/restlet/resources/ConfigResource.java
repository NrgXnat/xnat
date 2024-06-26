/*
 * web: org.nrg.xnat.restlet.resources.ConfigResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.XFTTable;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.editscript.DicomEdit;
import org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtils;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

@Slf4j
public class ConfigResource extends SecureResource {
    private static final String PROJECT_ID   = "PROJECT_ID";
    private static final String TOOL_NAME    = "TOOL_NAME";
    private static final String PATH_TO_FILE = "PATH_TO_FILE";
    private static final String REASON       = "REASON";

    private static final String[] configColumns = {"tool", "path", "project", "user", "create_date", "reason", "contents", "unversioned", "version", "status"};
    private static final String[] listColumns   = {"tool"};

    private final String projectId;
    private final String toolName;
    private final String reason;
    private       String path;

    // TODO: if we start using projectdata_info instead of id in config service:
    // private final long projectId;

    private final ConfigService configService;

    public ConfigResource(Context context, Request request, Response response) {
        super(context, request, response);

        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.TEXT_XML));

        configService = XDAT.getConfigService();

        //handle url here
        projectId = (String) getRequest().getAttributes().get(PROJECT_ID);
        toolName = (String) getRequest().getAttributes().get(TOOL_NAME);
        reason = (String) getRequest().getAttributes().get(REASON);
        path = getFullConfigPath();
    }

    @SuppressWarnings("CommentedOutCode")
    @Override
    public Representation represent(Variant variant) throws ResourceException {

        final UserI user = getUser();
        try {
            final MediaType mt = overrideVariant(variant);

            //handle query variables
            final boolean getHistory     = "getHistory".equalsIgnoreCase(getQueryVariable("action"));
            Integer       version        = null;
            final boolean meta           = isQueryVariableTrueHelper(getQueryVariable("meta"));
            final boolean contents       = isQueryVariableTrueHelper(getQueryVariable("contents"));
            final boolean acceptNotFound = isQueryVariableTrueHelper(getQueryVariable("accept-not-found"));

            try {
                version = Integer.parseInt(getQueryVariable("version"));
            } catch (Exception ignored) {
            }

            XFTTable table = new XFTTable();

            //check access, almost copy-paste code in the PUT method.
            if (!StringUtils.isBlank(projectId)) {
                final XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(projectId, user, false);
                if (project == null) {
                    final String message = String.format("The requested project %s does not exist.", projectId);
                    log.info(message);
                    getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, message);
                    throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, message);
                }
                if (!Permissions.canRead(user, "xnat:subjectData/project", projectId)) {
                    final String message = String.format("User %s can not access project %s", user.getUsername(), projectId);
                    log.warn(message);
                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, message);
                    throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, message);
                }
            }

            final List<Configuration> configurations = new ArrayList<>();
            final List<String>        list           = new ArrayList<>();
            final List<String>        tools;

            if (StringUtils.isBlank(toolName) && StringUtils.isBlank(path) && StringUtils.isBlank(projectId)) {
                //  /REST/config
                tools = configService.getTools();
                if (tools != null) {
                    list.addAll(tools);  //addAll is not null safe!
                }
            } else if (StringUtils.isBlank(toolName) && StringUtils.isBlank(path)) {
                //  /REST/projects/{PROJECT_ID}/config
                tools = configService.getTools(Scope.Project, projectId);

                if (tools != null) {
                    list.addAll(tools);  //addAll is not null safe!
                }
            } else if (StringUtils.isBlank(path)) {
                //  /REST/projects/{PROJECT_ID}/config/{TOOL_NAME}  or    /REST/config/{TOOL_NAME}
                final List<Configuration> l = StringUtils.isBlank(projectId)
                                              ? configService.getConfigsByTool(toolName, Scope.Site, null)
                                              : configService.getConfigsByTool(toolName, Scope.Project, projectId);
                if (l != null) {
                    configurations.addAll(l);  //addAll is not null safe.
                }
            } else {
                fixAnonPath();

                final boolean isSiteWide = StringUtils.isBlank(projectId);
                if (getHistory) {
                    //   /REST/config/{TOOL_NAME}/{PATH_TO_FILE}&action=getHistory  or  /REST/projects/{PROJECT_ID}/config/{TOOL_NAME}/{PATH_TO_FILE}&action=getHistory
                    final List<Configuration> foundConfigs = isSiteWide
                                                             ? configService.getHistory(toolName, path, Scope.Site, null)
                                                             : configService.getHistory(toolName, path, Scope.Project, projectId);
                    if (foundConfigs != null) {
                        configurations.addAll(foundConfigs);  //addAll is not null safe.
                    }
                } else {
                    if (version == null) {
                        Configuration configuration = null;
                        //   /REST/config/{TOOL_NAME}/{PATH_TO_FILE}  or  /REST/projects/{PROJECT_ID}/config/{TOOL_NAME}/{PATH_TO_FILE}
                        if (isSiteWide) {
                            configuration = configService.getConfig(toolName, path, Scope.Site, null);
                        } else {
                            try {
                                configuration = configService.getConfig(toolName, path, Scope.Project, projectId);
                                if (configuration == null && isQueryVariableTrue("defaultToSiteWide")) {
                                    //if project specific config is missing, allow fail over to site wide config
                                    configuration = configService.getConfig(toolName, path);
                                }
                            } catch (Exception e) {
                                // assume project config is missing
                                if (isQueryVariableTrue("defaultToSiteWide")) {
                                    //if project specific config is missing, allow fail over to site wide configService.getConfig(toolName, path));
                                    configuration = configService.getConfig(toolName, path);
                                }
                            }
                        }
                        if (configuration != null) {
                            configurations.add(configuration);
                        }
                    } else {
                        //   /REST/config/{TOOL_NAME}/{PATH_TO_FILE}&version={version}  or  /REST/projects/{PROJECT_ID}/config/{TOOL_NAME}/{PATH_TO_FILE}&version={version}
                        configurations.add(isSiteWide ? configService.getConfigByVersion(toolName, path, version, Scope.Site, null) : configService.getConfigByVersion(toolName, path, version, Scope.Project, projectId));
                    }
                    // we now react to the meta and contents parameters. if we're here, there is zero or 1 configuration added to the array.
                    // if contents=true, just send the contents as a string.
                    // if meta=true, zero out contents and just send the configuration metadata.
                    // if meta=true && contents==true, send teh configuration as-is.
                    // if meta=false && contents==false, this is the same as not specifying either in the querystring. So, just act as if they didn't.
                    if (contents && !meta) {
                        Configuration c = configurations.size() > 0 ? configurations.get(0) : null;
                        if (c == null || "disabled".equals(c.getStatus())) {
                            final String message = String.format("Config not found for user %s and project %s on tool [%s] path [%s]", user.getUsername(), projectId, toolName, path);
                            log.debug(message);
                            if (acceptNotFound) {
                                getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
                            } else {
                                getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, message);
                                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, message);
                            }
                        } else {
                            return new StringRepresentation(c.getContents());
                        }
                    } else if (meta && !contents) {
                        Configuration c = configurations.size() > 0 ? configurations.get(0) : null;
                        if (c != null) {
                            c.setConfigData(null);
                        }
                    }
                }
            }

            //This is a little weird. Above this line, we populate one of 2 lists (either tools (strings) or configurations)
            //Below this line we render the one list that got created. if no list got created, we render a 404

            if (list.size() > 0) { //"tool"
                //if we generated a listing of tools, represent those.
                table.initTable(listColumns);
                for (String s : list) {
                    if (s != null) {
                        String[] scriptArray = {s};
                        table.insertRow(scriptArray);
                    }
                }
                return representTable(table, mt, new Hashtable<>());

            } else if (configurations.size() > 0 && configurations.get(0) != null) {
                //we generated a list of configurations, so represent those.
                table.initTable(configColumns);  //"tool","path","project","user","create_date","reason","contents", "unversioned", "version", "status"};
                for (Configuration c : configurations) {
                    if (c != null) {
                        //TODO: Since ConfigService is using projectdata_info Long instead of the Project Name String, then we may have to convert
                        //the long id back to a project name string. Luckily, here we already have the project name (passed in)
                        //If you ever have to do that, it would look something like this:
                        //	String projectId;
                        //	List<XnatProjectdata> projects = XnatProjectdata.getXnatProjectdatasByField("xnat:projectData/projectdata_info", new Long(c.getProject()), user,false);
                        //	if(projects.size() < 1){
                        //		projectId = "DELETED";
                        //	} else {
                        //		XnatProjectdata match = projects.get(0);
                        //		projectId = match.getId();
                        //	}

                        String[] scriptArray = {
                                c.getTool(),
                                c.getPath(),
                                projectId,
                                c.getXnatUser(),
                                c.getCreated().toString(),
                                c.getReason(),
                                c.getContents(),
                                Boolean.toString(c.isUnversioned()),
                                Integer.toString(c.getVersion()),
                                c.getStatus()
                        };
                        table.insertRow(scriptArray);
                    }
                }
                return representTable(table, mt, new Hashtable<>());
            } else {
                //if we fell through to here, nothing existed at the supplied URI
                final String message = String.format("Couldn't find config for user %s and project %s on tool [%s] path [%s]", user.getUsername(), projectId, toolName, path);
                log.debug(message);
                if (acceptNotFound) {
                    getResponse().setStatus(Status.SUCCESS_NO_CONTENT, message);
                    return representTable(table, mt, new Hashtable<>());
                } else {
                    getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, message);
                    throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, message);
                }
            }
        } catch (Exception e) {
            if (e instanceof ResourceException) {
                throw (ResourceException) e;
            }
            final String message = String.format("An error occurred retrieving the config for user %s and project %s on tool [%s] path [%s]", user.getUsername(), projectId, toolName, path);
            log.debug(message, e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, message);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, message);
        }
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public void handlePut() {
        /*
         * PUT is idempotent: if the network is botched and the client is not sure whether his request made it through,
         * it can just send it a second (or 100th) time, and it is guaranteed by the HTTP spec that this has exactly the
         * same effect as sending once.
         */
        final UserI user = getUser();
        try {
            //check access, almost copy-paste code in the GET method.
            if (!((StringUtils.isNotBlank(projectId) && Permissions.canDelete(user, "xnat:subjectData/project", projectId)) || Roles.isSiteAdmin(user))) {
                log.warn("User {} can not modify config for project {}", user.getUsername(), projectId);
                getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User does not have privileges to modify config for this project");
                return;
            }

            fixAnonPath();

            final Representation entity    = getRequest().getEntity();
            final MediaType      mediaType = entity != null ? entity.getMediaType() : null;

            final Map<String, String> jsonParams;
            final String              status;
            if (entity != null && mediaType != null && mediaType.equals(MediaType.APPLICATION_JSON)) {
                jsonParams = getSerializer().deserializeJson(entity.getText(), SerializerService.TYPE_REF_MAP_STRING_STRING);
                status = jsonParams.get("status");
            } else {
                jsonParams = null;
                status = getQueryVariable("status");
            }

            final boolean hasStatus      = StringUtils.isNotBlank(status);
            final boolean hasBodyContent = entity != null && entity.getAvailableSize() > 0 || jsonParams != null && jsonParams.containsKey("contents");

            if (!hasStatus && !hasBodyContent) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "You must specify either the status querystring parameter or a configuration object in the request body.");
                return;
            }

            if (hasStatus && !StringUtils.equalsAnyIgnoreCase(status, "enabled", "true", "disabled", "false")) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Only valid values for the status flag are enabled or true and disabled or false: " + status);
                return;
            }

            //if this is JUST a status update, do it and return
            if (hasStatus && !hasBodyContent) {
                handleStatus(status, user.getUsername());
                // If we handled the status and there was no content posted, i.e.
                // no change to the configuration's contents, then we're done, OK.
                getResponse().setStatus(Status.SUCCESS_OK);
                return;
            }

            // This lets you post empty content to a configuration. Note that this
            // can only be done to a configuration where you are not changing the
            // status, since a status change operation with no body presumes that
            // all you wanted to do was change the status and returns OK (see lines
            // immediately above here).
            final String contents = getBodyContents(jsonParams);
            if (StringUtils.isBlank(contents)) {
                throw new ConfigServiceException("No contents provided");
            }

            final String isUnversionedParam = getQueryVariable("unversioned");

            // If there is a previous configuration check to see if it's enabled and if its contents equals the new contents. If so, just return success.
            // Do not update the configuration for puts are idempotent.
            final Configuration prevConfig = StringUtils.isBlank(projectId) ? configService.getConfig(toolName, path) : configService.getConfig(toolName, path, Scope.Project, projectId);
            final Status        response   = prevConfig == null ? Status.SUCCESS_CREATED : Status.SUCCESS_OK;

            // If there's not a previous config, or the previous configuration is not enabled, or the contents have changed...
            if (prevConfig == null ||
                !hasStatus && !StringUtils.equalsAnyIgnoreCase(prevConfig.getStatus(), "enabled", "true") ||
                hasStatus && !StringUtils.equalsAnyIgnoreCase(prevConfig.getStatus(), status) ||
                !contents.equals(prevConfig.getContents())) {
                //save/update the configuration
                final Configuration configuration;
                if (StringUtils.isBlank(isUnversionedParam)) {
                    configuration = configService.replaceConfig(user.getUsername(), reason, toolName, path, contents, StringUtils.isBlank(projectId) ? Scope.Site : Scope.Project, projectId);
                } else {
                    configuration = configService.replaceConfig(user.getUsername(), reason, toolName, path, Boolean.parseBoolean(isUnversionedParam), contents, StringUtils.isBlank(projectId) ? Scope.Site : Scope.Project, projectId);
                }
                if (hasStatus && !StringUtils.equals(status, configuration.getStatus())) {
                    handleStatus(status, user.getUsername());
                }
                if (projectId == null && StringUtils.equals(DicomEdit.ToolName, toolName)) {
                    DefaultAnonUtils.invalidateSitewideAnonCache();
                }
            }
            getResponse().setStatus(response);
        } catch (ConfigServiceException e) {
            log.error("Configuration service error replacing config for user {} and project {} on tool [{}] path [{}]", user.getUsername(), projectId, toolName, path);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
        } catch (Exception e) {
            log.error("Unknown error replacing config for user {} and project {} on tool [{}] path [{}]", user.getUsername(), projectId, toolName, path);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
        }
    }

    @Override
    public void handleDelete() {
        //check access, almost copy-paste code in the GET method.
        final UserI user = getUser();
        try {
            if (StringUtils.isBlank(projectId)) {
                if (!Roles.isSiteAdmin(user)) {
                    final String message = String.format("User %s is not an administrator and can't disable the configuration setting %s for the tool %s", user.getUsername(), path, toolName);
                    log.info(message);
                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, message);
                    return;
                }
                configService.disable(user.getLogin(), "Disabling this setting", toolName, path);
            } else {
                if (!(Permissions.canDelete(user, "xnat:subjectData/project", projectId) || Roles.isSiteAdmin(user))) {  //Users should be able to delete project config if they have project edit permissions or are site admins but are otherwise forbidden.
                    final String message = String.format("User %s can not access project %s to modify configuration setting %s for the tool %s", user.getUsername(), projectId, path, toolName);
                    log.info(message);
                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, message);
                    return;
                }
                configService.disable(user.getLogin(), "Disabling this setting", toolName, path, Scope.Project, projectId);
            }
        } catch (Exception e) {
            log.error("Unknown error deleting config for user {} and project {} on tool [{}] path [{}]: {}", user.getUsername(), projectId, toolName, path, e.getMessage());
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
        }

    }

    private String getBodyContents(@Nullable Map<String, String> jsonParams)
            throws FileUploadException, ClientException, IOException {
        if (jsonParams != null) {
            return jsonParams.get("contents");
        } else {
            List<FileWriterWrapperI> fws = getFileWriters();
            if (fws.size() == 0) {
                log.warn("No body contents");
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No body contents.");
                return null;
            }

            if (fws.size() > 1) {
                log.info("Config contents are limited to a single file");
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Config contents are limited to a single file");
                return null;
            }

            FileWriterWrapperI fw = fws.get(0);

            //read the input stream into a string buffer.
            return IOUtils.toString(fw.getInputStream(), StandardCharsets.UTF_8);//modified to use IOUtils because old code was adding a line break to single line files which wasn't in the uploaded content... true should be true, not true\n
        }
    }

    private void fixAnonPath() {
        //This is a bit of a hack, but doing the proper fix would introduce risk in the anonymization feature.  Which would be better done in a feature release, then a bug fix release.
        //The anon feature pre-dated the config service, but was migrated to use the config service for storage of the anonymization script.
        //However, it *appears* that the 'path' being set when the anonymization file is added (DicomEdit.buildScriptPath) is incorrect.  It is has a / at the beginning of the path, whereas other scripts in the config service don't.
        //So the ConfigResource correctly creates the path without the / at the beginning, but that fails to match the entry stored in the service by DicomEdit.  DicomEdit should be fixed, but that would introduce a lot of headaches.
        //So, for now, we'll just hack ConfigResource to support the erroneous path in this one use case.
        if (toolName != null && StringUtils.equals("anon", toolName) && projectId != null && StringUtils.equals("projects/" + projectId, path)) {
            path = "/projects/" + projectId;
        }
    }

    //This method parses the URI and returns the "path" used for Configurations.
    private String getFullConfigPath() {
        String path = (String) getRequest().getAttributes().get(PATH_TO_FILE);

        //restlet matches the first part of the path and ignores the rest.
        //if path is not null, we need to see if there's anything at the end of the URL to add.
        if (path != null) {
            final String remainingPart = getRequest().getResourceRef().getRemainingPart();
            if (remainingPart != null) {
                path = path + remainingPart;
            }

            // Lop off any query string parameters.
            int index = path.indexOf('?');
            if (index > 0) {
                path = StringUtils.left(path, index);
            }
        }
        return path;
    }

    private void handleStatus(final String status, final String username) throws ConfigServiceException {
        if (StringUtils.equalsAnyIgnoreCase(status, "enabled", "true")) {
            if (StringUtils.isBlank(projectId)) {
                configService.enable(username, reason, toolName, path);
            } else {
                configService.enable(username, reason, toolName, path, Scope.Project, projectId);
            }
        } else {
            if (StringUtils.isBlank(projectId)) {
                configService.disable(username, reason, toolName, path);
            } else {
                configService.disable(username, reason, toolName, path, Scope.Project, projectId);
            }
            getResponse().setStatus(Status.SUCCESS_OK);
        }

        if (StringUtils.isBlank(projectId) && StringUtils.equals(toolName, DicomEdit.ToolName)) {
            DefaultAnonUtils.invalidateSitewideAnonCache();
        }
    }
}
