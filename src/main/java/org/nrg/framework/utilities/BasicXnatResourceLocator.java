/*
 * framework: org.nrg.framework.utilities.BasicXnatResourceLocator
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BasicXnatResourceLocator extends AbstractXnatResourceLocator {
    private BasicXnatResourceLocator(final String pattern) {
        super(pattern);
    }

    public static Resource getResource(final String pattern) throws IOException {
        return new BasicXnatResourceLocator(pattern).getResource();
    }

    public static List<Resource> getResources(final String pattern) throws IOException {
        return new BasicXnatResourceLocator(pattern).getResources();
    }

    public static String asString(final String pattern) throws IOException {
        final Resource resource = getResource(pattern);
        if (resource == null) {
            return null;
        }
        try (final Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }

    public static String asString(final Resource resource) throws IOException {
        try (final Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }
}
