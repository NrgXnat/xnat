/*
 * web: org.nrg.xnat.restlet.extensions.VerifyExtensionsRestlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.extensions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.xnat.restlet.XnatRestlet;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;

@XnatRestlet("/services/extensions/verify")
public class VerifyExtensionsRestlet extends SecureResource {
    private static final Log _log = LogFactory.getLog(VerifyExtensionsRestlet.class);

    public VerifyExtensionsRestlet(Context context, Request request, Response response) {
        super(context, request, response);
        this.getVariants().add(new Variant(MediaType.ALL));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (_log.isDebugEnabled()) {
            _log.debug("Passing a representation of the verify extensions restlet.");
        }

        return new StringRepresentation("This is to verify that dynamically configured restlets are functioning properly.");
    }
}
