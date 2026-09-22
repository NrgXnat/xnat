package org.nrg.dicom.mizer.objects;

import com.google.common.io.ByteStreams;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomOutputStream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * The one place a dataset carrying its file meta information becomes bytes.
 * <p>
 * Every serialization does the same things in the same order: split group 0002 back out of the
 * dataset ({@link Dcm4cheConvert#splitFmiAndDataset}, which also re-derives the media storage SOP
 * Class and Instance UIDs from the dataset, so a script that rewrote them stays consistent), write
 * both through a {@link DicomObjectFactory#BULK_DATA_BUFFER_SIZE} buffer -- dcm4che moves bulk
 * data 2 KB at a time, so an unbuffered target costs a system call per 2 KB -- on a stream opened
 * as Explicit VR Little Endian, the required syntax for the meta group, after which dcm4che
 * switches to the transfer syntax the meta group names; and finally merge the meta group back into
 * the dataset for whatever processing follows (XNAT-8719).
 * <p>
 * Callers differ only in parameters: a customizer that may stamp the meta group before it is
 * written, and, for an object whose tail was never parsed, a raw remainder to copy through after
 * the dataset. Keeping those as parameters of one implementation, rather than as two copies of the
 * algorithm, is what keeps the two anonymization paths writing identical bytes.
 */
public final class DicomObjectWriter {

    private DicomObjectWriter() {
    }

    /**
     * Writes the dataset, and closes <b>out</b>.
     *
     * @param datasetWithFmi the dataset with its file meta information merged in. On return the
     *                       (possibly customized) meta group is merged back into it.
     * @param out            where to write. Buffered here, so callers hand over the raw stream.
     * @param fmiCustomizer  adjusts the split-out file meta information before it is written, for
     *                       example to stamp the source AE title. May be null.
     * @param rawRemainder   the unparsed rest of a partially-read object, copied through byte for
     *                       byte after the dataset. May be null. Only meaningful after a partial
     *                       read, which is never of a Deflated object -- those are always parsed
     *                       whole -- so the copy never has to thread a deflater.
     *
     * @return the number of remainder bytes copied; 0 when no remainder was given.
     *
     * @throws IOException if writing fails.
     */
    public static long write(final Attributes datasetWithFmi, final OutputStream out,
                             final Consumer<Attributes> fmiCustomizer, final InputStream rawRemainder) throws IOException {
        final Dcm4cheConvert.SplitAttributes split = Dcm4cheConvert.splitFmiAndDataset(datasetWithFmi);
        if (fmiCustomizer != null) {
            fmiCustomizer.accept(split.fmi);
        }
        long copied = 0;
        final BufferedOutputStream buffered = new BufferedOutputStream(out, DicomObjectFactory.BULK_DATA_BUFFER_SIZE);
        try (final DicomOutputStream dos = new DicomOutputStream(buffered, UID.ExplicitVRLittleEndian)) {
            dos.writeDataset(split.fmi, split.onlyDataset);
            if (rawRemainder != null) {
                dos.flush();
                copied = ByteStreams.copy(rawRemainder, buffered);
            }
        }
        datasetWithFmi.addAll(split.fmi);
        return copied;
    }
}
