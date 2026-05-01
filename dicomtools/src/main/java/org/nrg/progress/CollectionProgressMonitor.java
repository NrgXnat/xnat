/*
 * DicomDB: org.nrg.progress.CollectionProgressMonitor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.progress;

import java.util.Collection;


/**
 * Wrapper for a ProgressMonitorI object monitoring an action on a Collection,
 * where the (initial) maximum size is simply the size of the collection.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class CollectionProgressMonitor extends IterableProgressUpdater implements ProgressUpdater {
    private int maxprogress;

    public CollectionProgressMonitor(final Collection<?> coll, final ProgressMonitorI pm, final int initprogress) {
        super(pm);
        this.progress = initprogress;
        this.maxprogress = coll.size() + initprogress;
    }

    public CollectionProgressMonitor(final Collection<?> coll, final ProgressMonitorI pm) {
        this(coll, pm, 0);
    }

    @Override
    public void initialize(final String message) {
        pm.setMinimum(0);
        pm.setMaximum(maxprogress);
        pm.setProgress(progress);
        pm.setNote(message);
    }

    @Override
    public void incrementMax() {
        pm.setMaximum(++maxprogress);
    }

    @Override
    public void incrementMax(final int inc) {
        maxprogress += inc;
        pm.setMaximum(maxprogress);
    }
}
