/*
 * org.nrg.xnat.restlet.XnatRestletExtensionList
 *
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * XNAT is an open-source project of the Neuroinformatics Research Group.
 * Released under the Simplified BSD.
 *
 * Last modified 1/19/16 3:49 PM
 */

package org.nrg.xnat.restlet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class XnatRestletExtensions {
    public XnatRestletExtensions(final Set<String> packages) {
        super();
        _packages.addAll(packages);
    }

    public int size() {
        return _packages.size();
    }

    public Set<String> getPackages() {
        return Collections.unmodifiableSet(_packages);
    }

    private final Set<String> _packages = new HashSet<>();
}
