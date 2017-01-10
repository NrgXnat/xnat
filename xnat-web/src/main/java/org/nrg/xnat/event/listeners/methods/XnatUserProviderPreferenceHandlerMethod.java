/*
 * web: org.nrg.xnat.event.listeners.methods.XnatUserProviderPreferenceHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.utils.XnatUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class XnatUserProviderPreferenceHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Autowired
    public XnatUserProviderPreferenceHandlerMethod(final List<XnatUserProvider> providers) {
        for (final XnatUserProvider provider : providers) {
            _providers.put(provider.getUserKey(), provider);
        }
    }

    @Override
    public List<String> getHandledPreferences() {
        return new ArrayList<>(_providers.keySet());
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        for (final String key : values.keySet()) {
            handlePreference(key, values.get(key));
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if (_providers.containsKey(preference)) {
            final XnatUserProvider provider = _providers.get(preference);
            if (!StringUtils.equals(value, provider.getLogin())) {
                try {
                    final UserI user = Users.getUser(value);
                    if (!Roles.isSiteAdmin(user)) {
                        _log.error("Can't set the {} user provider login name to {}, as that user is not a site administrator.", preference, value);
                        throw new NrgServiceRuntimeException(NrgServiceError.PermissionsViolation, value);
                    }
                } catch (UserNotFoundException e) {
                    throw new NrgServiceRuntimeException(NrgServiceError.UserNotFoundError, value);
                } catch (UserInitException e) {
                    throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred trying to retrieve the user " + value, e);
                }
                _log.info("Setting the {} user provider login name to {}", preference, value);
                provider.setLogin(value);
                provider.clearUserObject();//This clears out the old user object so that XnatUserProvider will update the user object based on the login we just set.
            } else {
                _log.error("Not changing the {} user provider login name to {}, it's already set to that.", preference, value);
            }
        } else {
            _log.error("Couldn't find a user provider with the name {}", preference);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(XnatUserProviderPreferenceHandlerMethod.class);

    private final Map<String, XnatUserProvider> _providers = Maps.newHashMap();
}
