/*
 * PrearcImporter: org.nrg.Untarrer
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Untar;

/**
 * Extracts contents of (optionally compressed) tar file.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class Untarrer extends Unpacker {
  private final Untar.UntarCompressionMethod method;
  private final Project project;

  /**
   * Create an Untarrer for the given compression method
   * @param desc compression method description
   * @param project associated Ant project
   */
  public Untarrer(final String desc, final Project project) {
    method = new Untar.UntarCompressionMethod();
    if (method.indexOfValue(desc) < 0)
      throw new IllegalArgumentException(desc + " is not a valid tar compression method");
    method.setValue(desc);
    this.project = project;
  }

  /**
   * Unpacks the tar file.  The underlying Ant task appears to lock the
   * source file, so we don't bother.
   * @param file tar file to be unpacked
   * @param destination destination directory
   */
  public final void unpack(final File file, final File destination) {
    publishStatus(file, "unpacking");

    final Untar untar = new Untar();
    untar.setProject(project);
    untar.setCompression(method);

    untar.setDest(destination == null ? file.getParentFile() : destination);
    untar.setSrc(file);
    untar.setOverwrite(false);
    try {
      untar.execute();
      file.delete();
      publishStatus(file, "unpacked");
    } catch (BuildException e) {
      e.printStackTrace();
      publishFailure(file, e.getMessage());
    }
  }
}
