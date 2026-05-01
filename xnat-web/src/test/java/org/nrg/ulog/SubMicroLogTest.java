/*
 * SessionBuilders: org.nrg.ulog.SubMicroLogTest
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

public class SubMicroLogTest {
  private final long LINE_SEPARATOR_LEN = System.getProperty("line.separator").length();
  private final File logFile = new File("SubMicroLogTest.log");
  private MicroLog superlog;
  private MicroLog log;
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    superlog = new FileMicroLog(logFile);
    log = new SubMicroLog(superlog);
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    logFile.delete();
  }

  /**
   * Test method for {@link org.nrg.ulog.SubMicroLog#close()}.
   */
  @Test
  public void testClose() throws IOException {
    log.log("foo");
    assertTrue(log.hasMessages());
    log.close();
    assertFalse(log.hasMessages());
    superlog.close();
    assertEquals("foo".length() + LINE_SEPARATOR_LEN, logFile.length());
  }

  /**
   * Test method for {@link org.nrg.ulog.SubMicroLog#hasMessages()}.
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
   * Test method for {@link org.nrg.ulog.SubMicroLog#log(java.lang.String)}.
   */
  @Test
  public void testLogString() throws IOException {
    log.log("foo"); // TODO: something nontrivial
  }

  /**
   * Test method for {@link org.nrg.ulog.SubMicroLog#log(java.lang.String, java.lang.Throwable)}.
   */
  @Test
  public void testLogStringThrowable() throws IOException {
    log.log("foo", new RuntimeException("yikes!")); // TODO: something nontrivial
  }
}
