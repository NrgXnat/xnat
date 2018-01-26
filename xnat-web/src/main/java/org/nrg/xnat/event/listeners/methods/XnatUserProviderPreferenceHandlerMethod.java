/*
 * web: org.nrg.xnat.event.listeners.methods.XnatUserProviderPreferenceHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class XnatUserProviderPreferenceHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    @Autowired
    public XnatUserProviderPreferenceHandlerMethod(final List<XnatUserProvider> providers) {
        super(getProviderKeys(providers));
        for (final XnatUserProvider provider : providers) {
            _providers.put(provider.getUserKey(), provider);
        }
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        if (_providers.containsKey(preference)) {
            final XnatUserProvider provider = _providers.get(preference);
            if (!StringUtils.equals(value, provider.getLogin())) {
                try {
                    final UserI user = Users.getUser(value);
                    if (!Roles.isSiteAdmin(user)) {
                        log.error("Can't set the {} user provider login name to {}, as that user is not a site administrator.", preference, value);
                        throw new NrgServiceRuntimeException(NrgServiceError.PermissionsViolation, value);
                    }
                } catch (UserNotFoundException e) {
                    throw new NrgServiceRuntimeException(NrgServiceError.UserNotFoundError, value);
                } catch (UserInitException e) {
                    throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred trying to retrieve the user " + value, e);
                }
                log.info("Setting the {} user provider login name to {}", preference, value);
                provider.setLogin(value);
            } else {
                log.error("Not changing the {} user provider login name to {}, it's already set to that.", preference, value);
            }
        } else {
            log.error("Couldn't find a user provider with the name {}", preference);
        }
    }

    private static String[] getProviderKeys(final List<XnatUserProvider> providers) {
        return Lists.transform(providers, new Function<XnatUserProvider, String>() {
            @Override
            public String apply(final XnatUserProvider provider) {
                return provider.getUserKey();
            }
        }).toArray(new String[0]);
    }

    private final Map<String, XnatUserProvider> _providers = new HashMap<>();
}
