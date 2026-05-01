/*
 * spawner: org.nrg.xnat.spawner.services.SpawnerResourceLocator
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.services;

import org.nrg.framework.utilities.AbstractXnatResourceLocator;

import java.util.List;

public class SpawnerResourceLocator extends AbstractXnatResourceLocator {
    public SpawnerResourceLocator() {
        super(SpawnerService.DEFAULT_SPAWNER_LOCATOR_PATTERN);
    }

    public SpawnerResourceLocator(final String pattern) {
        super(pattern);
    }

    public SpawnerResourceLocator(final List<String> patterns) {
        super(patterns);
    }
}
