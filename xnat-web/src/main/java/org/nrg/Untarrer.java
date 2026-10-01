/*
 * PrearcImporter: org.nrg.Untarrer
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Untar;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.zip.PathTraversalScanner;

/**
 * Extracts contents of (optionally compressed) tar file.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
@Slf4j
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
   *
   * @return true if the file was successfully unpacked, false otherwise.
   */
  public final boolean unpack(final File file, final File destination) {
    final File dest = destination == null ? file.getParentFile() : destination;
    publishStatus(file, "unpacking");

    // Reject the whole upload before extracting anything if any entry resolves outside of the destination --
    // the same atomic scan-then-extract guarantee Unzipper and TarUtils provide. Ant's Untar (1.9.12+/1.10.4+)
    // does refuse to write such entries itself, but it skips them silently and still reports success, so the
    // import would carry on with a partially-extracted archive and nobody would be told it was rejected.
    final List<String> fileEntries = new ArrayList<>();
    final List<String> unsafeEntries;
    try {
      unsafeEntries = findPathTraversalEntries(file, dest, fileEntries);
    } catch (IOException | BuildException e) {
      log.error("unable to unpack {}", file, e);
      publishFailure(file, "unable to unpack " + file + ": " + e.getMessage());
      return false;
    }
    if (!unsafeEntries.isEmpty()) {
      publishRejection(file, unsafeEntries);
      return false;
    }

    final Untar untar = new Untar();
    untar.setProject(project);
    untar.setCompression(method);

    untar.setDest(dest);
    untar.setSrc(file);
    untar.setOverwrite(false);

    // Untar never overwrites an existing file, so only the files that don't exist yet are the ones it extracts.
    // Note them now, so clearing the executable bit below never touches a file that was already there.
    final List<File> extractedFiles = new ArrayList<>();
    for (final String name : fileEntries) {
      final File extracted = new File(dest, name);
      if (!extracted.exists()) {
        extractedFiles.add(extracted);
      }
    }

    try {
      untar.execute();
      // Every extracted file is cleared of any executable bit its archive metadata may have carried, the same as
      // every other extraction entry point this project has (Unzipper, ZipUtils, TarUtils). Ant's Untar doesn't
      // currently apply a tar entry's mode to the file it writes, but that's Ant's behavior, not a guarantee.
      for (final File extracted : extractedFiles) {
        if (extracted.isFile()) {
          FileUtils.clearExecutable(extracted);
        }
      }
      file.delete();
      publishStatus(file, "unpacked");
      return true;
    } catch (BuildException e) {
        log.error("unable to unpack {}", file, e);
      publishFailure(file, e.getMessage());
      return false;
    }
  }

  /**
   * Scans every entry of the tar file and returns the names of any that do not resolve within the destination.
   * The stream is built exactly the way Ant's Untar builds it for extraction -- the same decompression, the same
   * TarInputStream and the same (default, platform) name encoding -- so the names checked here are the names
   * Untar will actually write. The name of every non-directory entry is also added to fileEntries, so the
   * caller knows which files the extraction will write.
   */
  private List<String> findPathTraversalEntries(final File file, final File destination, final List<String> fileEntries) throws IOException {
    try (final InputStream fis = new FileInputStream(file);
         final TarInputStream tis = new TarInputStream(method.decompress(file.getName(), new BufferedInputStream(fis)), null)) {
      return PathTraversalScanner.findPathTraversalEntries(() -> {
        final TarEntry te = tis.getNextEntry();
        if (te == null) {
          return null;
        }
        if (!te.isDirectory()) {
          fileEntries.add(te.getName());
        }
        return te.getName();
      }, destination);
    }
  }
}
