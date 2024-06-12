/*
 * framework: org.nrg.framework.io.FileWalkIteratorTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.io;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class FileWalkIteratorTest {
    /**
     * Test method for {@link FileWalkIterator#FileWalkIterator(File, EditProgressMonitor)}.
     */
    @Test
    public void testFileWalkIteratorFile() {
        final File root = new File(System.getProperty("test.root", System.getProperty("user.dir")));
        final Iterator<File> i = new FileWalkIterator(root, null);
        i.hasNext();
    }

    /**
     * Test method for {@link FileWalkIterator#FileWalkIterator(Iterator, EditProgressMonitor)}.
     */
    @Test
    public void testFileWalkIteratorIterator() {
        final Iterator<File> fileIterator = Arrays.asList(File.listRoots()).iterator();
        final Iterator<File> fileWalkIterator = new FileWalkIterator(fileIterator, null);
        fileWalkIterator.hasNext();
    }

    /**
     * Test method for {@link FileWalkIterator#hasNext()}.
     */
    @Test
    public void testHasNext() {
        final File root = new File(System.getProperty("test.root", System.getProperty("user.dir")));
        final Iterator<File> i = new FileWalkIterator(root, null);
        while (i.hasNext()) {
            assertTrue(i.hasNext());
            i.next();
        }
        try {
            i.next();
            fail("hasNext() returned false incorrectly");
        } catch (NoSuchElementException ignored) {}
    }

    /**
     * Test method for {@link FileWalkIterator#next()}.
     */
    @Test
    public void testNext() throws IOException {
        final File newDir = new File("target/test/FileWalkIterator_testNext");
        newDir.mkdirs();
        final File d1 = new File(newDir, "d1");
        d1.mkdir();
        final File f1 = new File(newDir, "f1");
        final File f2 = new File(d1, "f2");
        final Set<File> files = Sets.newHashSet();
        files.add(f1);
        files.add(f2);
        try {
            makeTestFile(f1);
            makeTestFile(f2);
            final Iterator<File> i = new FileWalkIterator(newDir, null);
            while (i.hasNext()) {
                final File f = i.next();
                assertTrue(f.isFile());
                files.remove(f);
            }
            assertTrue(files.isEmpty());
        } finally {
            f1.delete();
            f2.delete();
            d1.delete();
            newDir.delete();
        }
    }

    /**
     * Test method for {@link FileWalkIterator#remove()}.
     */
    @Test
    public void testRemove() throws IOException {
        final File newDir = new File("target/test/FileWalkIterator_testRemove");
        newDir.mkdirs();
        final File toDelete = new File(newDir, "delete_me");
        final File toKeep = new File(newDir, "not_me");
        try {
            makeTestFile(toDelete);
            makeTestFile(toKeep);
            assertTrue(toDelete.isFile());
            assertTrue(toKeep.isFile());
            int nRemoved = 0;
            final Iterator<File> i = new FileWalkIterator(newDir, null);
            while (i.hasNext()) {
                final File f = i.next();
                if (f.getName().equals(toDelete.getName())) {
                    i.remove();
                    nRemoved++;
                }
            }
            assertEquals(1, nRemoved);
            assertFalse(toDelete.isFile());
            assertTrue(toKeep.isFile());
        } finally {
            toDelete.delete();
            toKeep.delete();
            newDir.delete();
            new File("target/test").delete();
            new File("target").delete();
        }
    }

    private void makeTestFile(final File file) throws IOException {
        try (final FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(new byte[]{0, 0, 0, 0});
        }
    }
}
