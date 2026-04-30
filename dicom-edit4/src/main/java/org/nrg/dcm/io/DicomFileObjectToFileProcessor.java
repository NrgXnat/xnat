/*
 * DicomEdit: org.nrg.dcm.io.DicomFileObjectToFileProcessor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.io;

import com.google.common.base.Function;
import org.nrg.dcm.edit.DicomFileObjectProcessor;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
@SuppressWarnings("unused")
public final class DicomFileObjectToFileProcessor implements DicomFileObjectProcessor {
    private final Function<File,File> fn;

    /**
     * Saves each given DicomObject o with associated File f to some File f'
     * determined by a provided Function operating on the original File f.
     * The Iterable argument to reduce() should have as its first element
     * the original File f, and has its second element the DicomObject o.
     *
     * @param fn The function for processing files.
     */
    public DicomFileObjectToFileProcessor(final Function<File,File> fn) {
        this.fn = fn;
    }

    public Object process(final File f, final DicomObjectI o) throws IOException, MizerException {
        final File out = fn.apply(f);
        if (out != null) {
            try (final FileOutputStream fos = new FileOutputStream(out)) {
                 o.write(fos);
                 return out;
            }
        }
        return null;
    }
}
