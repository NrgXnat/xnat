/*
 * web: org.nrg.xnat.restlet.XnatRestletExtensions
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
