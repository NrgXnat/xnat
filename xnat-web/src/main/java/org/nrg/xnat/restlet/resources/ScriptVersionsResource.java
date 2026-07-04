/*
 * web: org.nrg.xnat.restlet.resources.ScriptVersionsResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.apache.commons.lang3.StringUtils;
import org.nrg.automation.entities.Script;
import org.nrg.automation.services.ScriptRunnerService;
import org.nrg.automation.services.ScriptService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.XFTTable;
import org.nrg.xft.security.UserI;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ScriptVersionsResource extends AutomationResource {

    public ScriptVersionsResource(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);

        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));

        _scriptService = XDAT.getContextService().getBean(ScriptService.class);
        _runnerService = XDAT.getContextService().getBean(ScriptRunnerService.class);

        _scriptId = (String) getRequest().getAttributes().get(SCRIPT_ID);

        final UserI user = getUser();

        // If the user isn't a site admin, there's a limited set of operations they are permitted to perform.
        if (!Roles.isSiteAdmin(user)) {
            // You can't put or post or delete a script and you can't retrieve a specific script.
            if (StringUtils.isNotBlank(_scriptId)) {
                _log.warn(getRequestContext("User " + user.getLogin() + " attempted to access forbidden script trigger template resources"));
                response.setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Only site admins can view or update script resources.");
                throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "Only site admins can view or update script resources.");
            }
        }

        if (_log.isDebugEnabled()) {
            _log.debug(getRequestContext("Servicing script request for user " + user.getLogin()));
        }
    }

    @Override
    protected String getResourceType() {
        return Script.class.getSimpleName();
    }

    @Override
    protected String getResourceId() {
        return _scriptId;
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        final MediaType mediaType = overrideVariant(variant);

        if (StringUtils.isNotBlank(_scriptId)) {
            try {
                List<String> versions = _scriptService.getVersions(_scriptId);

                // have to check if it's null, or else it will return a StringRepresentation containing the word null instead of a 404
                if (versions != null) {
                    return new StringRepresentation(toJson(versions), mediaType);
                } else {
                    return null;
                }
            } catch (IOException e) {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "An error occurred marshalling the script data to JSON", e);
            }
        } else {
            // They're asking for list of available scripts, so give them that.
            return listScripts(mediaType);
        }
    }

    /**
     * Lists the scripts at the specified scope and entity ID.
     *
     * @return A representation of the scripts available at the specified scope and entity ID (if specified).
     */
    private Representation listScripts(final MediaType mediaType) {
        Hashtable<String, Object> params = new Hashtable<>();

        ArrayList<String> columns = new ArrayList<>();
        columns.add("Script ID");
        columns.add("Language");
        columns.add("Description");
        columns.add("Version");

        XFTTable table = new XFTTable();
        table.initTable(columns);

        final List<Script> scripts = _scriptService.getAll();
        for (final Script script : scripts) {
            table.insertRowItems(script.getScriptId(),
                    script.getLanguage(),
                    script.getDescription());
//                    script.getScriptVersion());
        }

        return representTable(table, mediaType, params);
    }

    private List<Script> getScripts() {
        return _runnerService.getScripts(_scriptId);
    }

    private static final Logger _log = LoggerFactory.getLogger(ScriptVersionsResource.class);

    private static final String SCRIPT_ID = "SCRIPT_ID";

    private final ScriptService _scriptService;
    private final ScriptRunnerService _runnerService;
    private final String _scriptId;
}
