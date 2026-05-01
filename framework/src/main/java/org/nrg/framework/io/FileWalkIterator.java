/*
 * framework: org.nrg.framework.io.FileWalkIterator
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.framework.io;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
@Slf4j
public class FileWalkIterator implements Iterator<File> {
    private final static RuntimeException NEXT_NOT_CALLED = new IllegalStateException("next() not called");
    private final static int              MAX_PATH_LENGTH = 2048;
    private static final Iterator<File>   EMPTY           = Collections.emptyIterator();

    private final Queue<File> files = new LinkedList<>();
    private final Queue<File> dirs  = new LinkedList<>();

    private final EditProgressMonitor progress;
    private final Iterator<File>      input;
    private       Object              current = NEXT_NOT_CALLED;
    private       int                 count;
    private       int                 known;

    public FileWalkIterator(final Iterator<File> input, final EditProgressMonitor progress) {
        this.progress = progress;
        this.input    = input;
        count         = 0;
        if (null != this.progress) {
            progress.setMinimum(0);
            progress.setMaximum(known = input.hasNext() ? 1 : 0);
        }
    }

    public FileWalkIterator(final File root, final EditProgressMonitor progress) {
        this(EMPTY, progress);
        (root.isDirectory() ? dirs : files).add(root);
        if (null != progress) {
            progress.setMaximum(known = 1);
        }
    }

    @SuppressWarnings("unused")
    public FileWalkIterator(final Iterable<File> files, final EditProgressMonitor progress) {
        this(files.iterator(), progress);
    }

    private void prepareQueue() {
        while (files.isEmpty()) {
            if (dirs.isEmpty()) {
                if (input.hasNext()) {
                    final File file = input.next();
                    if (file.isDirectory()) {
                        dirs.add(file);
                    } else {
                        files.add(file);
                        return;
                    }
                } else {
                    // no more elements available
                    return;
                }
            }
            assert files.isEmpty();
            assert !dirs.isEmpty();

            final File   directory = dirs.remove();
            final File[] contents  = directory.listFiles();
            if (null == contents) {
                log.info("directory {} returned null listing", directory);
            } else {
                log.debug("reading directory {}", directory);
                if (null != progress) {
                    progress.setNote("reading contents of " + directory.getPath());
                    progress.setMaximum(known += contents.length);
                }

                for (final File f : contents) {
                    if (f.isDirectory()) {
                        if (f.getPath().length() > MAX_PATH_LENGTH) {
                            log.info("skipping deep directory {} to avoid symlink cycle", f);
                            count++;
                        } else {
                            dirs.add(f);
                        }
                    } else {
                        files.add(f);
                    }
                }
            }

            ++count;
            if (null != progress) {
                progress.setProgress(count);
            }
        }
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        prepareQueue();
        return !files.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public File next() {
        prepareQueue();
        if (files.isEmpty()) {
            throw new NoSuchElementException();
        } else {
            count++;
            if (null != progress) {
                progress.setProgress(count);
            }
            return (File) (current = files.remove());
        }
    }

    @SuppressWarnings("unused")
    public File nextFile() {
        return next();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        if (NEXT_NOT_CALLED == current) {
            throw NEXT_NOT_CALLED;
        } else {
            final File f = (File) current;
            assert !f.isDirectory();
            if (!f.delete()) {
                log.debug("failed to delete {}", f);
            }
        }
    }

    @SuppressWarnings("unused")
    public int getCount() {
        return count;
    }
}
