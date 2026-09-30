package org.nrg.dicom.mizer.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomStreamException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.objects.AnonymizationResultReject;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.Mizer;
import org.nrg.dicom.mizer.service.MizerContext;
import org.nrg.transaction.operations.CallOnFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Apply the given script to the given dicom file on the filesystem.
 * From a PHI point-of-view all exceptions throws by this function
 * should be considered fatal.
 *
 * The dicom data is read from the given file, changed headers and potentially changed pixels are
 * written to a staging file. The given file is replaced with the
 * staging file if the anonymization process is successful.
 *
 * NOTE: The record and scriptId arguments indicate whether to record the application of this
 * script in the DICOM header and what the ID of the script is. For that reason if "record" is
 * false, the scriptId isn't checked and allowed to be null. If "record" is true, the "scriptId"
 * cannot be null and results in a runtime exception.
 *
 * This is really janky, but Java doesn't have pattern-matching on tuples and wrapping "record" and
 * "scriptId" into an object makes this function more opaque and harder to use.
 **/
@Slf4j
public class AnonymizeCallOnFileWithPixels extends CallOnFile<AnonymizationResult> {
    AnonymizeCallOnFileWithPixels(final File dicomFile, final Mizer mizer, final MizerContext mizerContext) {
        _dicomFile = dicomFile;
        _mizer = mizer;
        _mizerContext = mizerContext;
    }

    // The dicom data is read from the given file, but the changed pixel data and
    // headers are written to the staging file WorkOnCopyOp hands us. The given file
    // is replaced with the staging file if the anonymization process is successful.
    @Override
    public AnonymizationResult call() throws Exception {
        log.info("Preparing to anonymize file {} to {}", _dicomFile.getAbsolutePath(), getFile().getAbsolutePath());
        // Constructed from the File, not a stream, so that bulk data references, where there are
        // any, point at _dicomFile rather than at a spooled copy.
        final DicomObjectI dicomObject = DicomObjectFactory.newInstance(_dicomFile, bulkDataHandlingFor(_dicomFile.length()));
        try {
            final AnonymizationResult result = _mizer.anonymize(dicomObject, _mizerContext);
            // The staging file exists only once there is a successful result to write into it. A
            // rejection or an error is reported through the result, and WorkOnCopyOp then leaves the
            // source alone; a staging file opened beforehand would be empty and would replace it.
            if (!(result instanceof AnonymizationResultReject) && !(result instanceof AnonymizationResultError)) {
                try (final FileOutputStream output = new FileOutputStream(getFile())) {
                    result.getDicomObject().write(output);
                }
            }
            result.releaseObjectFromMemory();
            return result;
        } catch (DicomStreamException e) {
            throw new IOException(e);
        } finally {
            // Safe here and not before: the write above has completed, and WorkOnCopyOp does not
            // replace _dicomFile until we return.
            dicomObject.releaseScratchFiles();
        }
    }

    /**
     * How to read an object of the given size. Up to {@link #WHOLE_READ_LIMIT} it is read onto the
     * heap in one pass, as it always was. A larger one keeps its bulk data -- pixel data, and any
     * other binary value over 64 bytes -- in the file, as references that the write streams from
     * there: that bounds the heap an anonymization takes, and it is the only way to read an object
     * over 2 GB at all, since dcm4che cannot fit such a value in a byte[]. References cost a small
     * object dearly, though. The write opens the file again for every value, and for every fragment
     * of encapsulated pixel data, which on network storage is a round trip or more each: an MR
     * object with two private CSA headers opens its file four times instead of once.
     */
    static DicomInputStream.IncludeBulkData bulkDataHandlingFor(final long length) {
        return length <= WHOLE_READ_LIMIT ? DicomInputStream.IncludeBulkData.YES : DicomInputStream.IncludeBulkData.URI;
    }

    /** The largest object read onto the heap, where each concurrent anonymization holds one. */
    static final long WHOLE_READ_LIMIT = 64L << 20;

    private final File         _dicomFile;
    private final Mizer        _mizer;
    private final MizerContext _mizerContext;
}
