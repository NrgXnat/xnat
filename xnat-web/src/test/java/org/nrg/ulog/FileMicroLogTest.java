/*
 * SessionBuilders: org.nrg.ulog.FileMicroLogTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.ulog;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileMicroLogTest {
  private final long LINE_SEPARATOR_LEN = System.getProperty("line.separator").length();
  private final File logFile = new File("FileMicroLogTest.log");
  private MicroLog log;
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    log = new FileMicroLog(logFile);
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    logFile.delete();
  }


  /**
   * Test method for {@link org.nrg.ulog.FileMicroLog#close()}.
   */
  @Test
  public void testClose() throws IOException {
    log.log("foo");
    assertTrue(log.hasMessages());
    log.close();
    assertFalse(log.hasMessages());
    assertEquals("foo".length() + LINE_SEPARATOR_LEN, logFile.length());
  }

  /**
   * Test method for {@link org.nrg.ulog.FileMicroLog#hasMessages()}.
   */
  @Test
  public void testHasMessages() throws IOException {
    assertFalse(log.hasMessages());
    log.log("foo");
    assertTrue(log.hasMessages());
    log.log("bar");
    assertTrue(log.hasMessages());
  }

  /**
   * Test method for {@link org.nrg.ulog.FileMicroLog#log(java.lang.String)}.
   */
  @Test
  public void testLogString() throws IOException {
    log.log("foo");
    log.close();
    assertEquals("foo".length() + LINE_SEPARATOR_LEN, logFile.length());
  }

  /**
   * Test method for {@link org.nrg.ulog.FileMicroLog#log(java.lang.String, java.lang.Throwable)}.
   */
  @Test
  public void testLogStringThrowable() throws IOException {
    log.log("bar", new RuntimeException("yikes!")); // TODO: something nontrivial
  }

  /**
   * Test method for {@link org.nrg.ulog.FileMicroLog#toString()}.
   */
  @Test
  public void testToString() throws IOException {
    final File logFile = new File("test.ulog");
    final MicroLog log = new FileMicroLog(logFile);
    assertEquals(logFile.getPath(), log.toString());
    logFile.delete();
  }

}
