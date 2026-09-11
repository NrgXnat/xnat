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
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nrg.framework.status.LoggerStatusReporter;
import org.nrg.framework.status.StatusMessage;

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

  /**
   * A path-traversal ("zip-slip") entry must be reported as a FAILED status -- never COMPLETED -- so a caller
   * (PrearcImporter) doesn't treat the unpack as a success and go on to delete the original archive and queue the
   * destination for import.
   */
  @Test
  public final void testUnpackRejectsPathTraversalEntry() throws Exception {
    final File zip = new File(workingDir, "malicious.zip");
    try (final FileOutputStream fos = new FileOutputStream(zip);
         final ZipOutputStream zos = new ZipOutputStream(fos)) {
      zos.putNextEntry(new ZipEntry("../evil.txt"));
      zos.write("payload".getBytes(StandardCharsets.UTF_8));
      zos.closeEntry();
    }

    final File destination = new File(workingDir, "dest");
    final List<StatusMessage> statuses = new ArrayList<>();

    final Unzipper u = new Unzipper();
    u.addStatusListener(statuses::add);
    u.unpack(zip, destination);

    assertFalse("the traversal entry must never be written outside the destination",
                new File(workingDir, "evil.txt").exists());
    assertTrue("a FAILED status must be published",
               statuses.stream().anyMatch(m -> m.getStatus() == StatusMessage.Status.FAILED));
    assertFalse("no COMPLETED status may be published once extraction failed",
                statuses.stream().anyMatch(m -> m.getStatus() == StatusMessage.Status.COMPLETED));
  }
}
