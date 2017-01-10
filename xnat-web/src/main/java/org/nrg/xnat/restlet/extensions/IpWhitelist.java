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
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
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
        } else if (request.getMethod() == Method.PUT && !request.isEntityAvailable()) {
            getResponse().setStatus(Status.CLIENT_ERROR_PRECONDITION_FAILED, "You must provide a configuration for whitelisted IP addresses.");
        } else {
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
            String whitelist = getRequest().getEntity().getText();
            List<String> addresses = new ArrayList<>(Arrays.asList(whitelist.split("[\\s,]+")));
            for (String localhost : XDAT.getLocalhostIPs()) {
                if (!addresses.contains(localhost)) {
                    addresses.add(localhost);
                }
            }
            XDAT.getConfigService().replaceConfig(getUser().getLogin(), "", XDAT.IP_WHITELIST_TOOL, XDAT.IP_WHITELIST_PATH, Joiner.on("\n").join(addresses));
        } catch (IOException e) {
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e, "Error occurred trying to handle the incoming data");
            _log.error("Error occurred trying to handle the incoming data", e);
        } catch (ConfigServiceException e) {
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e, "Error occurred writing to the configuration service");
            _log.error("Error occurred writing to the configuration service", e);
        }
    }


    private static final Logger _log = LoggerFactory.getLogger(IpWhitelist.class);
            }
