/*
 * web: org.nrg.xnat.event.listeners.methods.AbstractScopedXnatPreferenceHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import org.nrg.xdat.preferences.EventTriggeringAbstractPreferenceBean;
import org.nrg.xdat.security.user.XnatUserProvider;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public abstract class AbstractScopedXnatPreferenceHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    protected AbstractScopedXnatPreferenceHandlerMethod(final String... handledPreferences) {
        super(handledPreferences);
    }

    protected AbstractScopedXnatPreferenceHandlerMethod(final XnatUserProvider userProvider, final String... handledPreferences) {
        super(userProvider, handledPreferences);
    }

    protected AbstractScopedXnatPreferenceHandlerMethod(final Class<? extends EventTriggeringAbstractPreferenceBean> beanClass, final String... handledPreferences) {
        this(Collections.<Class<? extends EventTriggeringAbstractPreferenceBean>>singletonList(beanClass), null, handledPreferences);
    }

    protected AbstractScopedXnatPreferenceHandlerMethod(final List<Class<? extends EventTriggeringAbstractPreferenceBean>> beanClasses, final String... handledPreferences) {
        this(beanClasses, null, handledPreferences);
    }

    protected AbstractScopedXnatPreferenceHandlerMethod(final Class<? extends EventTriggeringAbstractPreferenceBean> beanClass, final XnatUserProvider userProvider, final String... handledPreferences) {
        this(Collections.<Class<? extends EventTriggeringAbstractPreferenceBean>>singletonList(beanClass), userProvider, handledPreferences);
    }

    protected AbstractScopedXnatPreferenceHandlerMethod(final List<Class<? extends EventTriggeringAbstractPreferenceBean>> beanClass, final XnatUserProvider userProvider, final String... handledPreferences) {
        super(beanClass, userProvider, handledPreferences);
    }
}
