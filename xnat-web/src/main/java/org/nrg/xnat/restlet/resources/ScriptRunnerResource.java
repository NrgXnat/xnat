package org.nrg.xnat.restlet.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.nrg.automation.runners.ScriptRunner;
import org.nrg.automation.services.ScriptRunnerService;
import org.nrg.xdat.XDAT;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ScriptRunnerResource extends AutomationResource {

    public ScriptRunnerResource(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);

        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));

        _runnerService = XDAT.getContextService().getBean(ScriptRunnerService.class);
        _language = (String) getRequest().getAttributes().get(LANGUAGE);

        // Technically this shouldn't happen because the URL has language first then version, but why not?
        if (_log.isDebugEnabled()) {
            _log.debug(getRequestContext("Servicing script runner request for user " + user.getLogin()) + (StringUtils.isBlank(_language) ? " to get a list of available script runners" : " to get information about " + _language));
        }
    }

    @Override
    protected String getResourceType() {
        return ScriptRunner.class.getSimpleName();
    }

    @Override
    protected String getResourceId() {
        return _language;
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        final MediaType mediaType = overrideVariant(variant);

        try {
            if (StringUtils.isNotBlank(_language)) {
                if (!_runnerService.hasRunner(_language)) {
                    throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, String.format("No script runner found for %s", _language));
                }
                final String json = _serializer.writeValueAsString(_runnerService.getRunner(_language));
                return new StringRepresentation(json, mediaType);
            } else {
                final List<String> runners = _runnerService.getRunners();
                return new StringRepresentation(_serializer.writeValueAsString(runners), mediaType);
            }
        } catch (JsonProcessingException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "There was an error processing the script runners to JSON", e);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(ScriptRunnerResource.class);

    private static final String LANGUAGE = "LANGUAGE";

    private static final ObjectMapper _serializer = new ObjectMapper();
    private final ScriptRunnerService _runnerService;
    private final String _language;
}
