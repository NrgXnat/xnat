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
 * written to a temporary file. The given file is replaced with the
 * temporary file if the anonymization process is successful.
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
    // headers are written to a temporary file in the system's temp directory
    // in the "anon_backup" directory. The given file is replaced with the
    // temporary file if the anonymization process is successful. The "anon_backup" directory
    // is left in place.
    @Override
    public AnonymizationResult call() throws Exception {
        log.info("Preparing to anonymize file {} to {}", _dicomFile.getAbsolutePath(), getFile().getAbsolutePath());
        // Read with IncludeBulkData.URI so pixel data stays in _dicomFile and is streamed straight
        // through to the output on write. Reading it onto the heap costs a full copy of the object
        // per concurrent anonymization, and outright fails above 2 GB where dcm4che cannot fit the
        // value in a byte[]. Constructed from the File, not a stream, so the BulkData references
        // point at _dicomFile rather than at a spooled copy.
        final DicomObjectI dicomObject = DicomObjectFactory.newInstance(_dicomFile, DicomInputStream.IncludeBulkData.URI);
        try (final FileOutputStream output = new FileOutputStream(getFile())) {
            final AnonymizationResult result = _mizer.anonymize(dicomObject, _mizerContext);
            if (!(result instanceof AnonymizationResultReject) && !(result instanceof AnonymizationResultError)) {
                result.getDicomObject().write(output);
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

    private final File         _dicomFile;
    private final Mizer        _mizer;
    private final MizerContext _mizerContext;
}
