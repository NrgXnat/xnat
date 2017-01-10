/*
 * web: org.nrg.xnat.restlet.XnatRestletExtensionsBean
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class XnatRestletExtensionsBean {
    public XnatRestletExtensionsBean(final List<XnatRestletExtensions> extensions) {
        for (final XnatRestletExtensions extension : extensions) {
            _packages.addAll(extension.getPackages());
        }
    }

    public int size() {
        return _packages.size();
    }

    public Set<String> getPackages() {
        return Collections.unmodifiableSet(_packages);
    }

    private final Set<String> _packages = new HashSet<>();
}
