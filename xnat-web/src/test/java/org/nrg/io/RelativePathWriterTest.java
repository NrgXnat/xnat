/*
 * SessionBuilders: org.nrg.io.RelativePathWriterTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.io;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import junit.framework.TestCase;

public class RelativePathWriterTest extends TestCase {
  private final static String fileSeparatorPattern = "[\\/\\\\]";
  static {
    final Pattern sep = Pattern.compile(fileSeparatorPattern);
    if (!sep.matcher(File.separator).matches()) {
      throw new RuntimeException("unimplemented file separator " + File.separator);
    }
  }
  
  final File root = new File("target/test");
  final File adir = new File(root, "a");
  final File bdir = new File(adir, "b");
  final File cdir = new File(bdir, "c");
  final File dfile = new File(cdir, "d.txt");
  final String drelpath = "a/b/c/d.txt";

  RelativePathWriter writer = null;

  public void setUp() throws IOException {
    cdir.mkdirs();
    writer = new RelativePathWriter(root, dfile);
  }
  
  public void tearDown() {
    for (final File f : cdir.listFiles()) {
      f.delete();
    }
    cdir.delete();
    bdir.delete();
    adir.delete();
  }
  
  /**
   * Test method for {@link org.nrg.io.RelativePathWriter#close()}.
   */
  public void testClose() throws IOException {
    writer.flush();
    writer.close();
    try {
      writer.flush();
      fail("expected failed flush() on closed writer");
    } catch (IOException expected) {}
  }

  /**
   * Test method for {@link org.nrg.io.RelativePathWriter#getFullPath()}.
   */
  public void testGetFullPath() throws IOException {
    assertEquals(mergePath(root.getCanonicalPath(), drelpath), writer.getFullPath());
  }

  /**
   * Test method for {@link org.nrg.io.RelativePathWriter#getRelativePath()}.
   */
  public void testGetRelativePath() {
    assertEquals(useSystemPathSeparators("a/b/c/d.txt"), writer.getRelativePath());
  }

  private static String mergePath(final String base, final String relative) {
    final StringBuilder sb = new StringBuilder(base);
    for (int i = sb.length() - 1; i >= 0 && File.separatorChar == sb.charAt(i); i--) {
      sb.deleteCharAt(i);
    }
    sb.append(File.separatorChar);
    return join(sb, File.separator, relative.split("[/\\\\]")).toString();
  }
  
  private static String useSystemPathSeparators(final String s) {
    return join(File.separator, (Object[])s.split("[/\\\\]"));
  }
  
  
  private static <T> StringBuilder join(final StringBuilder sb, final String separator,
      final T...objects) {
    if (0 == objects.length) return sb;
    sb.append(objects[0]);
    for (int i = 1; i < objects.length; i++) {
      sb.append(separator);
      sb.append(objects[i]);
    }
    return sb;
  }
  
  private static <T> String join(final String separator, final T...objects) {
    return join(new StringBuilder(), separator, objects).toString();
  }
}
