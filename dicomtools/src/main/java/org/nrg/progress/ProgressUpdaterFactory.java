/*
 * DicomDB: org.nrg.progress.ProgressUpdaterFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.progress;

import java.util.Collection;


/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class ProgressUpdaterFactory {
    private static final ProgressUpdaterFactory factory = new ProgressUpdaterFactory();

    public static ProgressUpdaterFactory getFactory() { return factory; }

    public ProgressUpdater build(final Iterable<?> items,
            final ProgressMonitorI pm, final int initProgress) {
        if (null == pm) {
            return new NullProgressUpdater();
        } else if (items instanceof Collection) {
            return new CollectionProgressMonitor((Collection<?>)items, pm, 0);
        } else {
            return new IterableProgressUpdater(pm);
        }
    }

    public ProgressUpdater build(final Iterable<?> items,
            final ProgressMonitorI pm) {
        return build(items, pm, 0);
    }
}
