/*
 * dicomtools: org.nrg.dcm.io.DicomFileObjectIteratorTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.io;

import com.google.common.collect.Sets;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nrg.dcm.TestFiles;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class DicomFileObjectIteratorTest extends TestFiles {
    private File _sample;
    private File _sampleGz;

    @Before
    public void setUp() throws IOException {
        _sample = copySampleFileToTestTarget(_sampleDicomFile);
        _sampleGz = copySampleFileToTestTarget(_sampleDicomFileGzip);
    }

    @After
    public void tearDown() {
        if (null != _sample) {
            _sample.delete();
        }
        if (null != _sampleGz) {
            _sampleGz.delete();
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testIterator() {
        final Set<File>                              dicoms   = Sets.newHashSet(_sample, _sampleGz);
        final Iterable<File>                         files    = Arrays.asList(_sample.getParentFile(), _sample, new File("/etc/motd"), _sampleGz);
        final Iterator<Map.Entry<File, DicomObject>> iterator = new DicomFileObjectIterator(files);
        assertTrue(iterator.hasNext());
        final Map.Entry<File, DicomObject> e1 = iterator.next();
        assertTrue(dicoms.contains(e1.getKey()));
        dicoms.remove(e1.getKey());
        assertFalse(dicoms.contains(e1.getKey()));
        final DicomObject o1      = e1.getValue();
        final byte[]      pixels1 = o1.getBytes(Tag.PixelData);
        assertNotNull(pixels1);
        assertTrue(iterator.hasNext());
        final Map.Entry<File, DicomObject> e2 = iterator.next();
        assertTrue(dicoms.contains(e2.getKey()));
        final DicomObject o2      = e2.getValue();
        final byte[]      pixels2 = o2.getBytes(Tag.PixelData);
        assertFalse(e1.getKey().equals(e2.getKey()));
        assertTrue(Arrays.equals(pixels1, pixels2));
        assertFalse(iterator.hasNext());
        iterator.next();
        fail("returned normally from next() on empty iterator");
    }

    @Test
    public void testIteratorWithStopTag() {
        final Set<File>                              dicoms   = Sets.newHashSet(_sample, _sampleGz);
        final Iterable<File>                         files    = Arrays.asList(_sample.getParentFile(), _sample, new File("/etc/motd"));
        final Iterator<Map.Entry<File, DicomObject>> iterator = new DicomFileObjectIterator(files).setStopTag(0x00080032);
        assertTrue(iterator.hasNext());
        final Map.Entry<File, DicomObject> e1 = iterator.next();
        assertTrue(dicoms.contains(e1.getKey()));
        dicoms.remove(e1.getKey());
        assertFalse(dicoms.contains(e1.getKey()));
        final DicomObject o1 = e1.getValue();
        assertTrue(o1.contains(0x00080031));
        assertFalse(o1.contains(0x00080032));
        assertFalse(o1.contains(0x00080033));
        assertFalse(iterator.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testDeleteFromIterator() {
        final Set<File>                              dicoms   = Sets.newHashSet(_sample, _sampleGz);
        final Iterable<File>                         files    = Arrays.asList(_sample.getParentFile(), _sample, new File("/etc/motd"), _sampleGz);
        final Iterator<Map.Entry<File, DicomObject>> iterator = new DicomFileObjectIterator(files).setStopTag(Tag.PixelData);
        assertTrue(iterator.hasNext());
        try {
            iterator.remove();
            fail("returned without error from remove() on unstarted iterator");
        } catch (IllegalStateException ignored) {
        }

        final Map.Entry<File, DicomObject> e1 = iterator.next();
        assertTrue(dicoms.contains(e1.getKey()));
        assertTrue(e1.getKey().exists());
        iterator.remove();
        assertFalse(e1.getKey().exists());
        dicoms.remove(e1.getKey());
        assertFalse(dicoms.contains(e1.getKey()));
        assertTrue(iterator.hasNext());

        final Map.Entry<File, DicomObject> e2 = iterator.next();
        assertTrue(dicoms.contains(e2.getKey()));
        assertTrue(e2.getKey().exists());
        assertFalse(e1.getKey().equals(e2.getKey()));
        assertFalse(iterator.hasNext());

        iterator.next();
        fail("returned normally from next() on empty iterator");
    }


}
