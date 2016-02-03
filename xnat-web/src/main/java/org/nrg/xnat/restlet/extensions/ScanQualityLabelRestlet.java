/*
 * org.nrg.xnat.restlet.extensions.ScanQualityLabelRestlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 9/9/13 1:24 PM
 */
package org.nrg.xnat.restlet.extensions;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.nrg.xnat.restlet.XnatRestlet;
import org.nrg.xnat.restlet.representations.JSONObjectRepresentation;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.turbine.utils.ScanQualityUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@XnatRestlet({"/services/scan-quality-labels", "/services/scan-quality-labels/{PROJECT_ID}"})
public class ScanQualityLabelRestlet extends SecureResource {
    public static final String PARAM_PROJECT_ID = "PROJECT_ID";

    private static final Logger _log = LoggerFactory.getLogger(ScanQualityLabelRestlet.class);
    private final String _projectId;

    public ScanQualityLabelRestlet(Context context, Request request, Response response) {
        super(context, request, response);
        this.getVariants().add(new Variant(MediaType.ALL));
        _projectId = (String) getRequest().getAttributes().get(PARAM_PROJECT_ID);
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (_log.isDebugEnabled()) {
            _log.debug("Entering the scan quality label represent() method");
        }

        try {
            List<String> labels = ScanQualityUtils.getQualityLabels(_projectId, user);
            JSONObject json = new JSONObject();
            json.put(StringUtils.isBlank(_projectId) ? "site" : _projectId, labels);
            return new JSONObjectRepresentation(MediaType.APPLICATION_JSON, json);
        } catch (JSONException e) {
            throw new ResourceException(e);
        }
    }
}
