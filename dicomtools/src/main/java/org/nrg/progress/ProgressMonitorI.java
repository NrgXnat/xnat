/*
 * DicomDB: org.nrg.progress.ProgressMonitorI
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.progress;

/**
 * Interface for progress notification (e.g., for progress bars)
 * This is partially compatible with the Swing ProgressMonitor,
 * though it leaves out a few methods.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public interface ProgressMonitorI {
    void setMinimum(int min);
    void setMaximum(int max);
    void setProgress(int current);
    void setNote(String note);
    void close();
    boolean isCanceled();
}
