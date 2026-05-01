/*
 * SessionBuilders: org.nrg.io.ScanCatalogFileWriterFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.io;

import java.io.File;

public final class ScanCatalogFileWriterFactory
extends AbstractRelativePathWriterFactory implements RelativePathWriterFactory {
  public ScanCatalogFileWriterFactory(final File root) {
    super(root, "scan_%s_catalog.xml");
  }
}
