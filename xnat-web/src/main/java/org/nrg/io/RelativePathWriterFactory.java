/*
 * SessionBuilders: org.nrg.io.RelativePathWriterFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.io;

import java.io.File;
import java.io.IOException;


public interface RelativePathWriterFactory {
  /**
   * Creates a RelativePathWriter
   * @param relativeDir Directory that will contain the output file; this path is
   *    relative to some root directory that the implementation class knows about.
   * @param nameargs arguments used to build the output filename
   * @return a RelativePathWriter for the specified file
   * @throws IOException
   */
  public RelativePathWriter getWriter(File relativeDir, Object...nameargs) throws IOException;
  public RelativePathWriter getWriter(Object...nameargs) throws IOException;
}
