/*
 * SessionBuilders: org.nrg.io.AbstractRelativePathWriterFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.io;

import java.io.File;
import java.io.IOException;

public abstract class AbstractRelativePathWriterFactory implements
    RelativePathWriterFactory {
  private final File root;
  private final String format;
  
  protected AbstractRelativePathWriterFactory(final File root, final String format) {
    this.root = root;
    this.format = format;
  }
  
  /*
   * (non-Javadoc)
   * @see org.nrg.io.RelativePathWriterFactory#getWriter(java.io.File, java.lang.Object[])
   */
  public final RelativePathWriter getWriter(final File relativeDir, final Object...nameargs)
  throws IOException {
    final String name = format.formatted(nameargs);
    final File item;
    if (null == relativeDir) {  // TODO: or working directory?
      item = new File(name);
    } else {
      item = new File(relativeDir, name);
    }
    return new RelativePathWriter(root, item);
  }
  
  /*
   * (non-Javadoc)
   * @see org.nrg.io.RelativePathWriterFactory#getWriter(java.lang.Object[])
   */
  public final RelativePathWriter getWriter(final Object...nameargs)
  throws IOException {
    return getWriter(null, nameargs);
  }
}
