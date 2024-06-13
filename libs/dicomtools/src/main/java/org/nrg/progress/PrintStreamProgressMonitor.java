/*
 * DicomDB: org.nrg.progress.PrintStreamProgressMonitor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.progress;

import java.io.PrintStream;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class PrintStreamProgressMonitor implements ProgressMonitorI {
    private final PrintStream out;
    private String note = "";
    private int min = 0, max = 0;

    /**
     * Creates a progress monitor that prints to the submitted print stream.
     * @param out The output stream to use.
     */
    public PrintStreamProgressMonitor(final PrintStream out) {
        this.out = out;
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.ProgressMonitorI#close()
     */
    public void close() {
        out.println(note + " complete");
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.ProgressMonitorI#isCanceled()
     */
    public boolean isCanceled() { return false; }

    /* (non-Javadoc)
     * @see org.nrg.dcm.ProgressMonitorI#setMaximum(int)
     */
    public void setMaximum(final int max) { this.max = max; }

    /* (non-Javadoc)
     * @see org.nrg.dcm.ProgressMonitorI#setMinimum(int)
     */
    public void setMinimum(final int min) { this.min = min; }

    /* (non-Javadoc)
     * @see org.nrg.dcm.ProgressMonitorI#setNote(java.lang.String)
     */
    public void setNote(final String note) {
        this.note = note;
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.ProgressMonitorI#setProgress(int)
     */
    public void setProgress(final int current) {
        final StringBuilder sb = new StringBuilder(note);
        sb.append(" (");
        sb.append(current);
        sb.append("/");
        if (0 != min) {
            sb.append("[").append(min).append(",").append(max).append("]");
        } else {
            sb.append(max);
        }
        sb.append(")");
        out.println(sb);
    }
}
