/*
 * prefs: org.nrg.prefs.events.AbstractPreferenceHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.events;

import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Set;

/**
 * Provides a default implementation of the {@link #findHandledPreferences(Collection)} method. This checks whether any
 * of the preferences from the event to be handled are in the list of handled preferences for this method. This is a
 * simple string check: no regular expressions, partial comparisons, or wild cards are supported. To support more
 * complex matching, you can override this default implementation in your specific method implementation.
 */
public abstract class AbstractPreferenceHandlerMethod implements PreferenceHandlerMethod {
    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> findHandledPreferences(final Collection<String> preferences) {
        final Set<String> handled = Sets.newHashSet(preferences);
        handled.retainAll(getHandledPreferences());
        return handled;
    }
}
