package org.nrg.xapi.authorization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.nrg.prefs.events.PreferenceHandlerMethod;
import org.nrg.xapi.rest.Username;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Roles;
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
        //return !_restrictUserListAccessToAdmins
        //Switched to using XDAT.getSiteConfigPreferences to get the preference so that it will correctly update without Tomcat restart.
        //This should probably be refactored so that either this class no longer stores this preference, or it does, but there is a method
        //which handles updates to the preference and updates the value in this class. For now, I made the least invasive change.
        return !XDAT.getSiteConfigPreferences().getRestrictUserListAccessToAdmins()
               || Roles.isSiteAdmin(getUser())
               || getUsernames(getJoinPoint()).contains(getUsername());
    }

    @Override
    protected boolean considerGuests() {
        return false;
    }

    @Override
    public List<String> getToolIds() {
        return TOOL_ID;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public Set<String> findHandledPreferences(final Collection<String> preferences) {
        return PREFS_PATTERNS;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        handlePreference(RESTRICT_PREF, values.get(RESTRICT_PREF));
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        synchronized (RESTRICT_PREF) {
            _restrictUserListAccessToAdmins = Boolean.parseBoolean(value);
        }
    }

    private static final List<String> TOOL_ID        = Collections.singletonList(SiteConfigPreferences.SITE_CONFIG_TOOL_ID);
    private static final String       RESTRICT_PREF  = "restrictUserListAccessToAdmins";
    private static final List<String> PREFERENCES    = ImmutableList.copyOf(Collections.singletonList(RESTRICT_PREF));
    private static final Set<String>  PREFS_PATTERNS = ImmutableSet.copyOf(Collections.singletonList("^" + RESTRICT_PREF + "$"));

    private static boolean _restrictUserListAccessToAdmins;
}
