/*
 * PrearcImporter: org.nrg.UnzipperTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nrg.framework.status.LoggerStatusReporter;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class UnzipperTest {
  private final static File sourceDir = new File("src/test/data");
  private final static File workingDir = new File("target/test-data");
  private final static String baseName = "subdir1";
  private final static String textFileName = "file1.txt";
  private final static String suffix = ".zip";
  
  final Project project = new Project();
  
  @Before
  public final void createWorkingDir() {
//    assertFalse(workingDir.exists());
    workingDir.mkdirs();
    assertTrue(workingDir.isDirectory());
    
    final Copy copy = new Copy();
    copy.setProject(project);
    copy.setTodir(workingDir);

    copy.setFile(new File(sourceDir, baseName + suffix));
    copy.execute();
   }

  @After
  public final void removeWorkingDir() {
    final Delete delete = new Delete();
    delete.setProject(project);
    delete.setDir(workingDir);
    delete.execute();
    assertFalse(workingDir.exists());
  }

  /**
   * Test method for {@link org.nrg.Unzipper#unpack(java.io.File, java.io.File)}.
   */
  @Test
  public final void testUnpackFileFile() {
    final Unzipper u = new Unzipper();
    u.addStatusListener(new LoggerStatusReporter(Unzipper.class));
    final File zip = new File(workingDir, baseName + suffix);
    
    // Once with default destination
    final File sd = new File(workingDir, baseName);
    assertFalse(sd.exists());
    u.unpack(zip);
    assertTrue(sd.isDirectory());
    assertTrue(new File(sd, textFileName).isFile());

    // Once with specified destination
    final File target = new File(workingDir, "root");
    target.mkdirs();
    final File osd = new File(target, baseName);
    assertFalse(osd.exists());
    u.unpack(zip, target);
    assertTrue(osd.isDirectory());
    assertTrue(new File(osd, textFileName).isFile());
  }
}
