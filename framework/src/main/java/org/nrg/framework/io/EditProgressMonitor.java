/*
 * framework: org.nrg.framework.io.EditProgressMonitor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.framework.io;

/**
 * Interface for progress notification (e.g., for progress bars)
 * This is partially compatible with the Swing ProgressMonitor,
 * though it leaves out a few methods.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public interface EditProgressMonitor {
  void setMinimum(int min);
  void setMaximum(int max);
  void setProgress(int current);
  void setNote(String note);
  void close();
  boolean isCanceled();
}
