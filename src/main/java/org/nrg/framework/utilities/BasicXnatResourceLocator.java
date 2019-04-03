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
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BasicXnatResourceLocator extends AbstractXnatResourceLocator {

    private BasicXnatResourceLocator(final String pattern) {
        super(pattern);
    }

    public static Resource getResource(final String pattern) {
        return new BasicXnatResourceLocator(pattern).getResource();
    }

    public static List<Resource> getResources(final String pattern) throws IOException {
        return new BasicXnatResourceLocator(pattern).getResources();
    }
}
