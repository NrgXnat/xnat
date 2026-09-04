package org.nrg.dicom.mizer.service;

import java.io.File;
import java.io.IOException;

/**
 * Chooses where the anonymized version of a file is written before it replaces the original.
 * <p>
 * The replacement is a rename when the staging directory is on the same filesystem as the file,
 * and a copy when it is not. A resolver that knows the layout of the data volumes can therefore
 * make every in-place anonymization an atomic rename by staging on the volume the file lives on.
 */
@FunctionalInterface
public interface StagingDirectoryResolver {
    /**
     * @param dicomFile the file about to be anonymized in place.
     *
     * @return the directory to stage the anonymized version in. It need not exist yet.
     *
     * @throws IOException if the directory cannot be determined or created.
     */
    File resolve(final File dicomFile) throws IOException;

    /**
     * Stages under {@code java.io.tmpdir}, which is where anonymization always staged before a
     * resolver could be supplied. That is rarely the filesystem the data is on, so the replacement
     * is a copy.
     */
    StagingDirectoryResolver JAVA_IO_TMPDIR = dicomFile -> new File(System.getProperty("java.io.tmpdir"), "anon_backup");
}
