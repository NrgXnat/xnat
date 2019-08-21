/*
 * core: org.nrg.xdat.security.helpers.UserHelper
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.turbine.util.RunData;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.XDATUserHelperService;
import org.nrg.xdat.security.services.SearchHelperServiceI;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.nrg.framework.exceptions.NrgServiceError.ConfigurationError;

@Slf4j
public class UserHelper {
    public static final String USER_HELPER = "userHelper";

    /**
     * Returns the currently configured permissions service. You can customize the implementation returned by adding a
     * new implementation to the org.nrg.xdat.security.user.custom package (or a differently configured package). You
     * can change the default implementation returned via the security.userManagementService.default configuration
     * parameter.
     *
     * @return An instance of the {@link SearchHelperServiceI search helper service}.
     */
    public static SearchHelperServiceI getSearchHelperService() {
        if (_searchService == null) {
            try {
                List<Class<?>> classes = Reflection.getClassesForPackage(XDAT.safeSiteConfigProperty("security.searchHelperService.package", "org.nrg.xdat.search.helper.custom"));

                if (classes != null && classes.size() > 0) {
                    for (Class<?> clazz : classes) {
                        if (SearchHelperServiceI.class.isAssignableFrom(clazz)) {
                            _searchService = (SearchHelperServiceI) clazz.newInstance();
                        }
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IOException | IllegalAccessException e) {
                log.error("", e);
            }

            //default to XDATUserHelperService implementation (unless a different default is configured)
            if (_searchService == null) {
                try {
                    String className = XDAT.safeSiteConfigProperty("security.searchHelperService.default", "org.nrg.xdat.security.XDATSearchHelperService");
                    _searchService = (SearchHelperServiceI) Class.forName(className).newInstance();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    log.error("", e);
                }
            }
        }
        return _searchService;
    }

    /**
     * Returns the currently configured permissions service. You can customize the implementation returned by adding a
     * new implementation to the org.nrg.xdat.security.user.custom package (or a differently configured package). Change
     * the default implementation returned via the security.userManagementService.default configuration parameter.
     *
     * @return An instance of the {@link UserHelperServiceI user helper service}.
     */
    public static UserHelperServiceI getUserHelperService(final UserI user) {
        if (user == null) {
            throw new NrgServiceRuntimeException(ConfigurationError, "You can't construct an instance of the user helper class without passing in a user object, but I just got a null");
        }

        // MIGRATION: All of these services need to switch from having the implementation in the prefs service to autowiring from the context.
        if (_userHelperCtor == null) {
            try {
                _userHelperCtor = getUserHelperImplementationClass().getConstructor(UserI.class);
            } catch (NoSuchMethodException e) {
                throw new NrgServiceRuntimeException(ConfigurationError, "The specified user helper class must have a constructor that accepts the UserI interface: " + _userHelperCtor.getName());
            }
        }

        try {
            return _userHelperCtor.newInstance(user);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new NrgServiceRuntimeException(ConfigurationError, "An error occurred trying to create a new instance of the specified user helper class: " + _userHelperCtor.getName(), e);
        }
    }

    public static UserHelperServiceI getUserHelper(final RunData data) {
        return (UserHelperServiceI) data.getSession().getAttribute(USER_HELPER);
    }

    public static void setGuestUserHelper(final RunData data) throws UserNotFoundException, UserInitException {
        data.getSession().setAttribute(USER_HELPER, UserHelper.getUserHelperService(Users.getGuest()));
    }

    public static void setUserHelper(final HttpServletRequest request, final UserI user) {
        Users.clearCache(user);
        request.getSession().setAttribute(USER_HELPER, getUserHelperService(user));
    }

    private static Class<? extends UserHelperServiceI> getUserHelperImplementationClass() {
        final String packageName = XDAT.safeSiteConfigProperty("security.userHelperService.package", "org.nrg.xdat.user.helper.custom");
        try {
            final List<Class<?>> classes = Reflection.getClassesForPackage(packageName);

            if (classes != null && classes.size() > 0) {
                for (final Class<?> clazz : classes) {
                    if (UserHelperServiceI.class.isAssignableFrom(clazz)) {
                        return clazz.asSubclass(UserHelperServiceI.class);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            throw new NrgServiceRuntimeException(ConfigurationError, "Couldn't find the specified package for the user helper class: " + packageName);
        } catch (IOException e) {
            log.error("An unknown exception occurred", e);
        }

        //default to XDATUserHelperService implementation (unless a different default is configured)
        final String className = XDAT.safeSiteConfigProperty("security.userHelperService.default", XDATUserHelperService.class.getName());
        try {
            return Class.forName(className).asSubclass(UserHelperServiceI.class);
        } catch (ClassNotFoundException e) {
            throw new NrgServiceRuntimeException(ConfigurationError, "The configured user helper service class could not be found on the classpath: " + className);
        }
    }

    private static Constructor<? extends UserHelperServiceI> _userHelperCtor = null;
    private static SearchHelperServiceI                      _searchService  = null;
}
