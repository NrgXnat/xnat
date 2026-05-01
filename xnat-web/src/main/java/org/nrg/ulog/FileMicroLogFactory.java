/*
 * SessionBuilders: org.nrg.ulog.FileMicroLogFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.ulog;

import java.io.File;
import java.io.IOException;

public final class FileMicroLogFactory implements MicroLogFactory {
  private final File dir;
  
  public FileMicroLogFactory(final File dir) {
    this.dir = dir;
  }
  
  /* (non-Javadoc)
   * @see org.nrg.ulog.MicroLogFactory#getLog(java.lang.String)
   */
  public MicroLog getLog(String label) throws IOException {
    if (null == dir) {
      return new FileMicroLog(new File(label));
    } else {
      return new FileMicroLog(new File(dir, label));
    }
  }
}
