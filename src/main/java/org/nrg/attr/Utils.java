/**
 * $Id: Utils.java,v 1.1 2007/03/09 17:23:48 karchie Exp $
 * Copyright (c) 2006 Washington University
 */
package org.nrg.attr;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;


/**
 * Collection of utility functions more or less related to attribute processing
 * @author Kevin A. Archie <karchie@npg.wustl.edu>
 */
public final class Utils {
  private Utils() {}        // prevent instantiation
  
  public static class NotRootDir extends Exception {
    private static final long serialVersionUID = 1L;
    NotRootDir(final File notparent, final File file) {
      super("URI for " + file.getPath() + " cannot be constructed relative to " + notparent.getPath());
    }
  }
  
  /**
   * Generate a new filename, by appending an integer if necessary.
   * NOTE: Nothing is changed on the file system, so this is not an
   * atomic operation and, in principle, someone could create a
   * file with our reserved name before we get a chance to.
   *
   * @param parent The directory that will contain the new file
   * @param name Base name for the new file
   * @param suffix File suffix; number is placed between the base name and this suffix.
   *   If null, the number is just placed after the base name.
   * @return File record for the new file
   */
  public static File getUnique(final File parent, final String name, final String suffix) {
    final String nameFormat = "%1$s_%2$d";
    File f = new File(parent, name);
    for (int i = 1; f.exists(); i++) {
      final String trialname;
      if (suffix == null) {
        trialname = String.format(nameFormat, name, i);
      } else {
        final int loc = name.lastIndexOf(suffix);
        if (loc >= 0) {
          trialname = String.format(nameFormat, name.substring(0, loc), i) + suffix;
        } else {
          trialname = String.format(nameFormat, name, i);
        }
      }
      f = new File(parent, trialname);
    }
    return f;
  }

  
  /**
   * Generate a new filename, by appending an integer if necessary.
   * NOTE: Nothing is changed on the file system, so this is not an
   * atomic operation and, in principle, someone could create a
   * file with our reserved name before we get a chance to.
   *
   * @param parent The directory that will contain the new file
   * @param name Base name for the new file
   * @return File record for the new file
   */
  public static File getUnique(final File parent, final String name) {
    return getUnique(parent, name, null);
  }
  

  public static String getRelativeURI(final File root, final File file) throws IOException,NotRootDir {
    final char URI_SEPARATOR_CHAR = '/';
    final String filePath = file.getCanonicalPath();
    
    int relativeStart;
    if (root == null) {
      relativeStart = 0;
    } else {
    final String rootPath = root.getCanonicalPath();
    
    if (!filePath.startsWith(rootPath))
      throw new NotRootDir(root, file);
    
    relativeStart = rootPath.length() + 1;
    }
    
    while (filePath.charAt(relativeStart) == File.separatorChar)
      relativeStart++;
    
    return filePath.substring(relativeStart).replace(File.separatorChar, URI_SEPARATOR_CHAR);
  }
  
  
  public final static class MaybeNumericStringComparator implements Comparator<String> {
    public int compare(final String o1, final String o2) {
      try {
        final int i1 = Integer.parseInt(o1);
        final int i2 = Integer.parseInt(o2);
        return i1 - i2;
      } catch (NumberFormatException e) {
        return o1.compareTo(o2);
      }
    }
    
    @Override
    public boolean equals(final Object o) {
      return o instanceof MaybeNumericStringComparator;
    }
  }
}
