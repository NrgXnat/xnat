/*
 * DicomDB: org.nrg.progress.IterableProgressUpdater
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.progress;


/**
 * ProgressUpdater wrapper for a ProgressMonitorI that monitors an action
 * on an Iterable, where we don't know the size and thus can't reasonably
 * determine a maximum progress value.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class IterableProgressUpdater implements ProgressUpdater {
    protected final ProgressMonitorI pm;
    protected int progress = 0;

    public IterableProgressUpdater(final ProgressMonitorI pm) {
        this.pm = pm;
    }

    public void close() {
        pm.close();
    }

    public void initialize(final String message) {
        pm.setNote(message);
    }

    public void incrementMax() {}
    public void incrementMax(int inc) {}

    public void incrementProgress() {
        pm.setProgress(++progress);
    }

    public boolean isCanceled() {
        return pm.isCanceled();
    }

    public void setNote(final String note) {
        pm.setNote(note);
    }
}
