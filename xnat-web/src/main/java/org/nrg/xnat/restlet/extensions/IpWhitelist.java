/*
 * web: org.nrg.xnat.restlet.extensions.IpWhitelist
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.extensions;

import com.google.common.base.Joiner;

import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xnat.restlet.XnatRestlet;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.*;
import org.restlet.routing.*;
import org.restlet.representation.*;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@XnatRestlet("/services/ipwhitelist")
public class IpWhitelist extends SecureResource {

    public IpWhitelist(Context context, Request request, Response response) {
        super(context, request, response);
        if (!Roles.isSiteAdmin(getUser())) {
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
        } else {
            // The empty-body check moved into handlePut(): under Restlet 2.6 isEntityAvailable() is
            // false for a form-encoded PUT even when the client sent a body (Servlet 6.0 3.1 — the
            // container only parses form bodies into the parameter map for POST), so testing it here
            // rejected valid requests with 412. Same defect as item 1-24 (/services/auth).
            this.getVariants().add(new Variant(MediaType.ALL));
        }
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (_log.isDebugEnabled()) {
            _log.debug("Entering the IP whitelist represent() method");
        }

        try {
            return new StringRepresentation(XDAT.getWhitelistConfiguration(getUser()));
        } catch (ConfigServiceException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
        }
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void handlePut() {
        try {
            String whitelist = getRequestBodyText();
            if (whitelist == null || whitelist.trim().isEmpty()) {
                getResponse().setStatus(Status.CLIENT_ERROR_PRECONDITION_FAILED, "You must provide a configuration for whitelisted IP addresses.");
                return;
            }
            List<String> addresses = new ArrayList<>(Arrays.asList(whitelist.split("[\\s,]+")));
            for (String localhost : XDAT.getLocalhostIPs()) {
                if (!addresses.contains(localhost)) {
                    addresses.add(localhost);
                }
            }
            XDAT.getConfigService().replaceConfig(getUser().getLogin(), "", XDAT.IP_WHITELIST_TOOL, XDAT.IP_WHITELIST_PATH, Joiner.on("\n").join(addresses));
        } catch (ConfigServiceException e) {
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e, "Error occurred writing to the configuration service");
            _log.error("Error occurred writing to the configuration service", e);
        }
    }


    private static final Logger _log = LoggerFactory.getLogger(IpWhitelist.class);
            }
