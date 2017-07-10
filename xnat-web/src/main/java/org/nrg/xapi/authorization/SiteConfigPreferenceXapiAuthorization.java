/*
 * web: org.nrg.xapi.authorization.SiteConfigPreferenceXapiAuthorization
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.authorization;

import com.fasterxml.jackson.databind.JsonNode;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.nrg.xnat.utils.FileUtils.nodeToList;

/**
 * Checks whether user can access the system user list.
 */
@Component
public class SiteConfigPreferenceXapiAuthorization extends AbstractXapiAuthorization {
    @Autowired
    public SiteConfigPreferenceXapiAuthorization(final SerializerService service) throws IOException {
        final Resource siteConfigAccess = RESOURCE_LOADER.getResource("classpath:META-INF/xnat/security/site-config-access.yaml");
        try (final InputStream inputStream = siteConfigAccess.getInputStream()) {
            final JsonNode nodes = service.deserializeYaml(inputStream);

            _public = nodeToList(nodes.get("public"));
            _authenticated = nodeToList(nodes.get("authenticated"));
        }
    }

    @Override
    protected boolean checkImpl() {
        final UserI user = getUser();

        // Admin can access them all so no need to look further.
        if (Roles.isSiteAdmin(user)) {
            return true;
        }

        final Object[] parameters = getJoinPoint().getArgs();

        // We only allow one parameter.
        if (parameters == null || parameters.length == 0 || parameters.length > 1) {
            return false;
        }

        if (parameters[0] instanceof List) {
            final List<?> preferences = (List<?>) parameters[0];
            for (final Object preference : preferences) {
                if (!checkPreference(user, (String) preference)) {
                    return false;
                }
            }
            return true;
        }

        if (parameters[0] instanceof String) {
            return checkPreference(user, (String) parameters[0]);
        }

        return checkPreference(user, parameters[0].toString());
    }

    private boolean checkPreference(final UserI user, final String preference) {
        return user.isGuest() ? _public.contains(preference) : _authenticated.contains(preference);
    }

    @Override
    protected boolean considerGuests() {
        return true;
    }

    private static final ResourceLoader RESOURCE_LOADER = new DefaultResourceLoader();

    private final List<String> _public;
    private final List<String> _authenticated;
}
