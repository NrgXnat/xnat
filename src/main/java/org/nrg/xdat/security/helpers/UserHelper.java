package org.nrg.xdat.security.helpers;

import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.services.SearchHelperServiceI;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

public class UserHelper {
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
                logger.error("", e);
            }

            //default to XDATUserHelperService implementation (unless a different default is configured)
            if (_searchService == null) {
                try {
                    String className = XDAT.safeSiteConfigProperty("security.searchHelperService.default", "org.nrg.xdat.security.XDATSearchHelperService");
                    _searchService = (SearchHelperServiceI) Class.forName(className).newInstance();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    logger.error("", e);
                }
            }
        }
        return _searchService;
    }

    public static UserHelperServiceI getUserHelperService(UserI user) {
        try {
            final Class<? extends UserHelperServiceI> clazz = getUserHelperImpl();
            final UserHelperServiceI service = clazz.newInstance();
            service.setUser(user);
            return service;
        } catch (InstantiationException e) {
            logger.error("", e);
            return null;
        } catch (IllegalAccessException e) {
            logger.error("", e);
            return null;
        }
    }

    /**
     * Returns the currently configured permissions service. You can customize the implementation returned by adding a
     * new implementation to the org.nrg.xdat.security.user.custom package (or a differently configured package). Change
     * the default implementation returned via the security.userManagementService.default configuration parameter.
     *
     * @return An instance of the {@link UserHelperServiceI user helper service}.
     */
    private static Class<? extends UserHelperServiceI> getUserHelperImpl() {
        // MIGRATION: All of these services need to switch from having the implementation in the prefs service to autowiring from the context.
        if (_userHelper == null) {
            try {
                final List<Class<?>> classes = Reflection.getClassesForPackage(XDAT.safeSiteConfigProperty("security.userHelperService.package", "org.nrg.xdat.user.helper.custom"));

                if (classes != null && classes.size() > 0) {
                    for (final Class<?> clazz : classes) {
                        if (UserHelperServiceI.class.isAssignableFrom(clazz)) {
                            _userHelper = clazz.asSubclass(UserHelperServiceI.class);
                        }
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                logger.error("", e);
            }

            //default to XDATUserHelperService implementation (unless a different default is configured)
            if (_searchService == null) {
                try {
                    final String className = XDAT.safeSiteConfigProperty("security.userHelperService.default", "org.nrg.xdat.security.XDATUserHelperService");
                    _userHelper = Class.forName(className).asSubclass(UserHelperServiceI.class);
                } catch (ClassNotFoundException e) {
                    logger.error("", e);
                }
            }
        }
        return _userHelper;
    }

    public static void setUserHelper(HttpServletRequest request, UserI user){
        Users.clearCache(user);
        request.getSession().setAttribute("userHelper", UserHelper.getUserHelperService(user));
    }

    private static final Logger logger = LoggerFactory.getLogger(UserHelper.class);

    private static Class<? extends UserHelperServiceI> _userHelper = null;
    private static SearchHelperServiceI _searchService = null;
}
