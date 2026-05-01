/*
 * PrearcImporter: org.nrg.AbstractRestructurerTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class AbstractRestructurerTest {
  private final static File[] dirs = {
    new File("dir"),
    new File("dir/d1"),
    new File("dir/d2"),
    new File("dir/d1/d3")
  };

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    for (final File f : dirs) {
      f.mkdirs();
    }
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test method for {@link org.nrg.AbstractRestructurer#pruneDirectoryTree(java.io.File)}.
   */
  @Test
  public final void testPruneDirectoryTreeEmpty() {
    assertTrue(dirs[0].isDirectory());
    AbstractRestructurer.pruneDirectoryTree(dirs[0]);
    assertFalse(dirs[0].exists());
  }
  
  @Test
  public final void testPruneDirectoryTreeNonempty() throws IOException {
    final File clog = new File(dirs[1], "clog");
    final FileWriter writer = new FileWriter(clog);
    writer.append(' ');
    writer.close();
    AbstractRestructurer.pruneDirectoryTree(dirs[0]);
    assertTrue(dirs[0].isDirectory());
    assertTrue(dirs[1].isDirectory());
    assertFalse(dirs[2].exists());
    assertFalse(dirs[3].exists());
    clog.delete();
    AbstractRestructurer.pruneDirectoryTree(dirs[0]);
    assertFalse(dirs[0].exists());
  }
}
