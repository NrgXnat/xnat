/*
 * web: org.nrg.xnat.event.listeners.methods.CachePathHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.utils.FileUtils;
import org.nrg.xnat.utils.ThreadAndProcessFileLock;
import org.springframework.stereotype.Component;

/**
 * Tells the classes that keep the cache path (the file locks put their lock files under it, the prearchive
 * its lock folders) to read it again when the cachePath preference changes. With the distributed events
 * plugin installed the change is relayed to every node as the same preference event.
 */
@Component
@Slf4j
public class CachePathHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    public CachePathHandlerMethod() {
        super("cachePath");
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        log.info("The cache path is now {}; lock files and cache folders will be resolved from it", value);
        ThreadAndProcessFileLock.cachePathChanged();
        FileUtils.cachePathChanged();
    }
}
