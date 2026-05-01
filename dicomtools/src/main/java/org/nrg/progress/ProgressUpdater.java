/*
 * DicomDB: org.nrg.progress.ProgressUpdater
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.progress;

import java.io.Closeable;

/**
 * Wrapper around ProgressMonitorI objects providing some additional abstraction.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public interface ProgressUpdater extends Closeable {
    void initialize(String message);
    void incrementMax();
    void incrementMax(int inc);
    void incrementProgress();
    boolean isCanceled();
    void setNote(String note);
}
