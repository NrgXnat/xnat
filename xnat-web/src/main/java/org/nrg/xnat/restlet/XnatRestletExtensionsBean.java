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
