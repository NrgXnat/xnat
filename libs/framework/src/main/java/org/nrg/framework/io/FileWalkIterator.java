/*
 * framework: org.nrg.framework.io.FileWalkIterator
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.framework.io;

import java.io.File;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;


/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class FileWalkIterator implements Iterator<File> {
    private final static RuntimeException NEXT_NOT_CALLED = new IllegalStateException("next() not called");
    private final static int PATHMAX = 2048;
    private static final Iterator<File> EMPTY = Iterators.forArray();

    private final Logger logger = LoggerFactory.getLogger(FileWalkIterator.class);
    private final Queue<File> files = Lists.newLinkedList(), dirs = Lists.newLinkedList();
    private final Iterator<File> input;
    private Object current = NEXT_NOT_CALLED;
    private int count, known;
    private final EditProgressMonitor progress;

    public FileWalkIterator(final Iterator<File> i, final EditProgressMonitor progress) {
        this.progress = progress;
        input = i;
        count = 0;
        if (null != progress) {
            progress.setMinimum(0);
            progress.setMaximum(known = i.hasNext() ? 1 : 0);
        }
    }

    public FileWalkIterator(final File root, final EditProgressMonitor progress) {
        this(EMPTY, progress);
        (root.isDirectory() ? dirs : files).add(root);
        if (null != progress) {
            progress.setMaximum(known = 1);
        }
    }

    public FileWalkIterator(final Iterable<File> i, final EditProgressMonitor progress) {
        this(i.iterator(), progress);
    }

    private void prepareQueue() {
        while (files.isEmpty()) {
            if (dirs.isEmpty()) {
                if (input.hasNext()) {
                    final File f = input.next();
                    if (f.isDirectory()) {
                        dirs.add(f);
                    } else {
                        files.add(f);
                        return;
                    }
                } else {
                    // no more elements available
                    return;
                }
            }
            assert files.isEmpty();
            assert !dirs.isEmpty();

            final File d = dirs.remove();
            final File[] contents = d.listFiles();
            if (null == contents) {
                logger.info("directory {} returned null listing", d);
            } else {
                logger.debug("reading directory {}", d);
                if (null != progress) {
                    progress.setNote("reading contents of " + d.getPath());
                    progress.setMaximum(known += contents.length);
                }

                for (final File f : contents) {
                    if (f.isDirectory()) {
                        if (f.getPath().length() > PATHMAX) {
                            logger.info("skipping deep directory {} to avoid symlink cycle", f);
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
            return (File)(current = files.remove());
        }
    }

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
            final File f = (File)current;
            assert !f.isDirectory();
            if (!f.delete()) {
                logger.debug("failed to delete {}", f);
            }
        }
    }

    public int getCount() {
        return count;
    }
}
