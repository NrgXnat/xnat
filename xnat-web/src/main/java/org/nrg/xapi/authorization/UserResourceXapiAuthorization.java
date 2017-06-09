package org.nrg.xapi.authorization;

import com.google.common.collect.ImmutableList;
import org.nrg.prefs.events.PreferenceHandlerMethod;
import org.nrg.xapi.rest.Username;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xnat.event.listeners.methods.AbstractSiteConfigPreferenceHandlerMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Checks whether user can access the system user list.
 */
@Component
public class UserResourceXapiAuthorization extends AbstractXapiAuthorization implements PreferenceHandlerMethod {
    @Autowired
    public UserResourceXapiAuthorization(final SiteConfigPreferences preferences) {
        _restrictUserListAccessToAdmins = preferences.getRestrictUserListAccessToAdmins();
    }

    /**
     * Tests whether the current user should be able to access the system user list:
     *
     * <ol>
     * <li>
     * If user list access is not restricted to administrators, then any authenticated user can access the list.
     * </li>
     * <li>
     * If user list access is restricted to administrators, this method checks for any {@link Username}-
     * annotated parameters for the {@link XapiRequestMapping}-annotated method. If the current user's name
     * appears in that list, that user can access the method in spite of not being an administrator.
     * </li>
     * <li>
     * Otherwise, only users with administrator privileges can access the list.
     * </li>
     * </ol>
     */
    @Override
    protected boolean checkImpl() {
        // Otherwise, verify that user list access is not restricted to admins OR this user is an admin OR this
        // user appears in any usernames specified in the method parameters.
        return !_restrictUserListAccessToAdmins
               || Roles.isSiteAdmin(getUser())
               || getUsernames(getJoinPoint()).contains(getUsername());
    }

    @Override
    protected boolean considerGuests() {
        return false;
    }

    @Override
    public List<String> getToolIds() {
        return _handlerProxy.getToolIds();
    }

    @Override
    public List<String> getHandledPreferences() {
        return _handlerProxy.getHandledPreferences();
    }

    @Override
    public Set<String> findHandledPreferences(final Collection<String> preferences) {
        return _handlerProxy.findHandledPreferences(preferences);
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        _handlerProxy.handlePreferences(values);
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        _handlerProxy.handlePreference(preference, value);
    }

    private static final String       RESTRICT_PREF = "restrictUserListAccessToAdmins";
    private static final List<String> PREFERENCES   = ImmutableList.copyOf(Collections.singletonList(RESTRICT_PREF));

    private final PreferenceHandlerMethod _handlerProxy = new AbstractSiteConfigPreferenceHandlerMethod() {
        @Override
        public List<String> getHandledPreferences() {
            return PREFERENCES;
        }

        @Override
        public void handlePreferences(final Map<String, String> values) {
            handlePreference(RESTRICT_PREF, values.get(RESTRICT_PREF));
        }

        @Override
        public void handlePreference(final String preference, final String value) {
            _restrictUserListAccessToAdmins = Boolean.parseBoolean(value);
        }
    };

    private boolean _restrictUserListAccessToAdmins;
}
