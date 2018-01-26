/*
 * web: org.nrg.xnat.event.listeners.methods.AbstractXnatPreferenceHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.prefs.events.AbstractPreferenceHandlerMethod;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xft.security.UserI;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;

@Slf4j
@Getter(AccessLevel.PROTECTED)
@Accessors(prefix = "_")
public abstract class AbstractXnatPreferenceHandlerMethod extends AbstractPreferenceHandlerMethod {
    protected AbstractXnatPreferenceHandlerMethod(final String... handledPreferences) {
        this(null, handledPreferences);
    }

    protected AbstractXnatPreferenceHandlerMethod(final XnatUserProvider userProvider, final String... handledPreferences) {
        _userProvider = userProvider;
        _handlerName = getClass().getName();

        if (handledPreferences.length == 0) {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "You must provide at least one preference to be handled by this method.");
        }

        initializePreferences(handledPreferences);
    }

    /**
     * Provides the method where handling specific to a particular preference value can be managed.
     *
     * @param preference The preference to be handled.
     * @param value      The new value of the preference.
     */
    protected abstract void handlePreferenceImpl(final String preference, final String value);

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getHandledPreferences() {
        return _preferences;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        final Set<String>  preferences        = values.keySet();
        final List<String> handledPreferences = getHandledPreferences();
        if (!Collections.disjoint(handledPreferences, preferences)) {
            for (final String preference : handledPreferences) {
                if (preferences.contains(preference)) {
                    final String value = values.get(preference);
                    log.info("Now handling preference {} with new value '{}'", preference, value);
                    handlePreferenceImpl(preference, value);
                }
            }
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if (getHandledPreferences().contains(preference)) {
            handlePreferenceImpl(preference, value);
        }
    }

    @Override
    public List<String> getToolIds() {
        return Collections.singletonList(SiteConfigPreferences.SITE_CONFIG_TOOL_ID);
    }

    protected UserI getAdminUser() {
        return _userProvider != null ? _userProvider.get() : null;
    }

    protected String getAdminUsername() {
        return _userProvider != null ? _userProvider.getLogin() : "";
    }

    /**
     * Gets all of the handled preferences and finds corresponding get methods on the subclass. For now this is only used
     * to log values when something's updated, but set methods could be cached as well and even remove need to have {@link #handlePreferenceImpl(String, String)}
     * method implementation.
     *
     * @param handledPreferences The array of handled preference names.
     */
    protected void initializePreferences(final String[] handledPreferences) {
        final List<String> preferences = new ArrayList<>(Arrays.asList(handledPreferences));

        //noinspection unchecked
        final Set<? extends Method> methods = ReflectionUtils.getMethods(getClass(), new Predicate<Method>() {
            @Override
            public boolean apply(final Method method) {
                final String name = method.getName();
                return StringUtils.startsWith(name, "get") && preferences.contains(StringUtils.uncapitalize(StringUtils.stripStart(name, "get")));
            }
        });

        for (final Method method : methods) {
            final String preference = StringUtils.uncapitalize(StringUtils.stripStart(method.getName(), "get"));
            getPreferenceMethods().put(preference, method);
        }

        getPreferences().addAll(getPreferenceMethods().keySet());

        final List leftovers = ListUtils.removeAll(preferences, getPreferences());
        if (!leftovers.isEmpty()) {
            log.info("Preferences were specified for the handler class {} with no corresponding getter. This isn't a big deal, but prevents diagnostic logging of stored values: {}", getHandlerName(), Joiner.on(", ").join(leftovers));
        }
    }

    /*
    TODO: Need to provide a way to only do some preferences in groups.
    protected void addGroupedPreferences(final String... group) {
        final List<String> groupList = Arrays.asList(group);
        for (final String preference : group) {
            _groupedPreferences.put(preference, groupList);
        }
    }
    */

    private final List<String>              _preferences        = new ArrayList<>();
    private final Map<String, Method>       _preferenceMethods  = new HashMap<>();
    private final Map<String, List<String>> _groupedPreferences = new HashMap<>();

    private final XnatUserProvider _userProvider;
    private final String           _handlerName;
}
