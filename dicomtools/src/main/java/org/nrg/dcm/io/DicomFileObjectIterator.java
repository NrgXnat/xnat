/*
 * dicomtools: org.nrg.dcm.io.DicomFileObjectIterator
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.io;

import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;


/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class DicomFileObjectIterator implements Iterator<Map.Entry<File, DicomObjectI>> {
    private final Iterator<File> files;
    private int stopTag = -1;
    File nextFile = null, lastFile = null;
    DicomObjectI nextObject = null;
    
    public DicomFileObjectIterator(final Iterator<File> files) {
        this.files = files;
    }
    
    public DicomFileObjectIterator(final Iterable<File> files) {
        this(files.iterator());
    }

    public DicomFileObjectIterator setStopTag(final int stopTag) {
        this.stopTag = stopTag;
        return this;
    }
    
    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        while (null == nextFile) {
            if (files.hasNext()) {
                final File f = files.next();
                try {
                    if (this.stopTag == -1) {
                        nextObject = DicomObjectFactory.newInstance(f);
                    } else {
                        nextObject = DicomObjectFactory.newInstance(f, this.stopTag);
                    }
                    nextFile = f;
                    return true;
                } catch (MizerException skip) {
                    continue;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public synchronized Entry<File,DicomObjectI> next() {
        final File currentFile = lastFile;
        lastFile = nextFile;
        if (hasNext()) {
            lastFile = nextFile;
            final File file = nextFile;
            final DicomObjectI doi = nextObject;
            nextFile = null;
            nextObject = null;
            return new Map.Entry<File, DicomObjectI>() {
                public File getKey() { return file; }
                public DicomObjectI getValue() { return doi; }
                @Override
                public DicomObjectI setValue(final DicomObjectI value) {
                    throw new UnsupportedOperationException("cannot set DICOM object value");
                }
            };
        } else {
            lastFile = currentFile;
            throw new NoSuchElementException();
        }
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        if (null == lastFile) {
            throw new IllegalStateException("no current file to delete");
        } else {
            lastFile.delete();
        }
    }
}
