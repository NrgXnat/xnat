/*
 * SessionBuilders: org.nrg.ulog.MicroLogFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.ulog;

import java.io.IOException;


public interface MicroLogFactory {
  public MicroLog getLog(String label) throws IOException;
}
