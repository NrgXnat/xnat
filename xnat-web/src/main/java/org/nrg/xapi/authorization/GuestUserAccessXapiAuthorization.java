package org.nrg.xapi.authorization;

import org.nrg.prefs.events.PreferenceHandlerMethod;
import org.nrg.xapi.rest.Username;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xnat.event.listeners.methods.AbstractScopedXnatPreferenceHandlerMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Checks whether user can access the system user list.
 */
@Component
public class GuestUserAccessXapiAuthorization extends AbstractXapiAuthorization implements PreferenceHandlerMethod {
    @Autowired
    public GuestUserAccessXapiAuthorization(final SiteConfigPreferences preferences) {
        _requireLogin = preferences.getRequireLogin();
    }

    /**
     * Tests whether the current user should be able to access any API calls that specify this authorization delegate. If
     * {@link SiteConfigPreferences#getRequireLogin()} is true, the user must be authenticated. If it's set to false, the
     * guest user can also access the annotated API call.
     */
    @Override
    protected boolean checkImpl() {
        // If login is not required, then return true, otherwise only allow if the user is not a guest.
        return !_requireLogin || !getUser().isGuest();
    }

    @Override
    protected boolean considerGuests() {
        return true;
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

    private final PreferenceHandlerMethod _handlerProxy = new AbstractScopedXnatPreferenceHandlerMethod("requireLogin") {
        @Override
        protected void handlePreferenceImpl(final String preference, final String value) {
            _requireLogin = Boolean.parseBoolean(value);
        }
    };

    private boolean _requireLogin;
}
