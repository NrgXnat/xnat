/*
 * web: org.nrg.xnat.event.listeners.methods.AbstractScopedXnatPreferenceHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import org.nrg.xdat.security.user.XnatUserProvider;

public abstract class AbstractScopedXnatPreferenceHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    protected AbstractScopedXnatPreferenceHandlerMethod(final String... preferences) {
        this(null, preferences);
    }

    protected AbstractScopedXnatPreferenceHandlerMethod(final XnatUserProvider userProvider, final String... preferences) {
        super(userProvider, preferences);
    }
}
