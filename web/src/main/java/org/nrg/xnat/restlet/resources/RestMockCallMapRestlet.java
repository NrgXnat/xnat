/*
 * web: org.nrg.xnat.restlet.resources.RestMockCallMapRestlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.XDAT;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.restlet.util.Template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestMockCallMapRestlet extends SecureResource {

    private final static ObjectMapper MAPPER = new ObjectMapper(new JsonFactory());

    private final String _call;
    private final Map<String, String> _callMap;
    private final List<Template> _templates;

    public static Map<String, String> getRestMockCallMap() {
        final String configuration = XDAT.getConfigService().getConfigContents("rest", "mockCallMap");
        if (StringUtils.isBlank(configuration)) {
            return new HashMap<>();
        }

        try {
            return MAPPER.readValue(configuration, SerializerService.TYPE_REF_MAP_STRING_STRING);
        } catch (IOException ignored) {
            return new HashMap<>();
        }
    }

    public RestMockCallMapRestlet(Context context, Request request, Response response) {
        super(context, request, response);

        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.TEXT_XML));

        _call = request.getResourceRef().toString().substring(request.getRootRef().toString().length());
        _callMap = getRestMockCallMap();
        _templates = new ArrayList<>(_callMap.size());
        for (String mapped : _callMap.keySet()) {
            _templates.add(new Template(mapped, Template.MODE_EQUALS));
        }
    }

    @Override
    public Representation represent(Variant variant) {
        Template template = getTemplate(_call);
        if (template == null) {
            this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        final Map<String, Object> variables = new HashMap<>();
        template.parse(_call, variables);
        String payload = _callMap.get(template.getPattern());
        for (Map.Entry<String, Object> mapping : variables.entrySet()) {
            payload = payload.replace("{" + mapping.getKey() + "}", mapping.getValue().toString());
        }
        return new StringRepresentation(payload);
    }

    private Template getTemplate(final String call) {
        for (Template template : _templates) {
            if (template.match(call) == call.length()) {
                return template;
            }
        }
        return null;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void handlePut() {
        /*
           * PUT is idempotent: if the network is botched and the client is not sure whether his request made it through,
           * it can just send it a second (or 100th) time, and it is guaranteed by the HTTP spec that this has exactly the
           * same effect as sending once.
           */
    }
}
