/*
 * DicomUtils: org.nrg.dcm.io.DicomFileObjectIterator
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.io;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputHandler;
import org.dcm4che2.io.StopTagInputHandler;
import org.nrg.dcm.DicomUtils;


/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class DicomFileObjectIterator implements Iterator<Map.Entry<File,DicomObject>> {
    private final Iterator<File> files;
    private DicomInputHandler inputHandler = null;
    File nextFile = null, lastFile = null;
    DicomObject nextObject = null;
    
    public DicomFileObjectIterator(final Iterator<File> files) {
        this.files = files;
    }
    
    public DicomFileObjectIterator(final Iterable<File> files) {
        this(files.iterator());
    }

    public DicomFileObjectIterator setInputHandler(final DicomInputHandler handler) {
        this.inputHandler = handler;
        return this;
    }
    
    public DicomFileObjectIterator setStopTag(final int stopTag) {
        return setInputHandler(new StopTagInputHandler(stopTag));
    }
    
    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        while (null == nextFile) {
            if (files.hasNext()) {
                final File f = files.next();
                try {
                    nextObject = DicomUtils.read(f, inputHandler);
                    nextFile = f;
                    return true;
                } catch (IOException skip) {
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
    public synchronized Entry<File,DicomObject> next() {
        final File currentFile = lastFile;
        lastFile = nextFile;
        if (hasNext()) {
            lastFile = nextFile;
            final File f = nextFile;
            final DicomObject o = nextObject;
            nextFile = null;
            nextObject = null;
            return new Map.Entry<File, DicomObject>() {
                public File getKey() { return f; }
                public DicomObject getValue() { return o; }
                public DicomObject setValue(final DicomObject value) {
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
