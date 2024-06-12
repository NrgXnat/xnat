/*
 * framework: org.nrg.framework.ui.SwingProgressMonitor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.framework.ui;

import org.nrg.framework.io.EditProgressMonitor;

import java.awt.Component;

import javax.swing.ProgressMonitor;

public class SwingProgressMonitor implements EditProgressMonitor {
    private final ProgressMonitor monitor;
    
    public SwingProgressMonitor(final ProgressMonitor monitor) {
        this.monitor = monitor;
    }
    
    public SwingProgressMonitor(final Component parent,
            final String message, final String note,
            final int min, final int max) {
        this(new ProgressMonitor(parent, message, note, min, max));
    }

    /* (non-Javadoc)
     * @see org.nrg.util.EditProgressMonitor#setMinimum(int)
     */
    public void setMinimum(final int min) {
        monitor.setMinimum(min);
    }

    /* (non-Javadoc)
     * @see org.nrg.util.EditProgressMonitor#setMaximum(int)
     */
    public void setMaximum(final int max) {
        monitor.setMaximum(max);
    }

    /* (non-Javadoc)
     * @see org.nrg.util.EditProgressMonitor#setProgress(int)
     */
    public void setProgress(final int current) {
        monitor.setProgress(current);
    }

    /* (non-Javadoc)
     * @see org.nrg.util.EditProgressMonitor#setNote(java.lang.String)
     */
    public void setNote(final String note) {
        monitor.setNote(note);
    }

    /* (non-Javadoc)
     * @see org.nrg.util.EditProgressMonitor#close()
     */
    public void close() {
        monitor.close();
    }

    /* (non-Javadoc)
     * @see org.nrg.util.EditProgressMonitor#isCanceled()
     */
    public boolean isCanceled() {
        return monitor.isCanceled();
    }
}
