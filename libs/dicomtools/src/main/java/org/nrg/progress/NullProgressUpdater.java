/*
 * DicomDB: org.nrg.progress.NullProgressUpdater
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.progress;

/**
 * ProgressUpdater wrapper for a null ProgressMonitorI.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class NullProgressUpdater implements ProgressUpdater {
    public void close() {}
    public void initialize(String message) {}
    public void incrementMax() {}
    public void incrementMax(int inc) {}
    public void incrementProgress() {}
    public boolean isCanceled() { return false; }
    public void setNote(String note) {}
}
