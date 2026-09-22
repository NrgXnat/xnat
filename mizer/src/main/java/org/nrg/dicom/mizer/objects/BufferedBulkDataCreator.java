package org.nrg.dicom.mizer.objects;

import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.BulkDataCreator;
import org.dcm4che3.io.DicomInputStream;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps bulk data off the heap for a {@link DicomInputStream} reading with
 * {@link DicomInputStream.IncludeBulkData#URI IncludeBulkData.URI}, choosing per value between two
 * strategies:
 * <ul>
 * <li><b>Reference into the source file</b>, when the stream has a URI and reads the file
 * directly: record where the value sits and skip past it, so the value is never copied. This is
 * what dcm4che itself does for a file-backed stream, except that the reference is a
 * {@link ReadAheadBulkData}, which reads back through a
 * {@link DicomObjectFactory#BULK_DATA_BUFFER_SIZE} buffer instead of 2 KB at a time.</li>
 * <li><b>Spool</b>, otherwise: copy the value into one spool file per stream through the same
 * buffer and reference it there. dcm4che's own spooling writes 2 KB at a time to an unbuffered
 * stream and opens one temporary file per value.</li>
 * </ul>
 * The choice is made from the stream's own state, never by the caller, because a reference into
 * the raw file is only honest when stream positions are file positions. A Deflated dataset is read
 * through an inflater, so its positions are inflated offsets that mean nothing against the file;
 * dcm4che's default creator refuses to reference in exactly that case, and this one spools it.
 * <p>
 * The creator owns every file it spools -- and nothing else, never a source file that references
 * point into. {@link #getSpoolFiles()} lists the spool files for whoever must delete them once
 * nothing holds a reference into them, and {@link #close()} flushes and closes the spool so that
 * everything written to it can be read back: close after the read that fills the dataset, before
 * anything follows its references.
 */
public final class BufferedBulkDataCreator implements BulkDataCreator, Closeable {

    private final File       spoolDirectory;
    private final List<File> spoolFiles = new ArrayList<>();

    private File         spoolFile;
    private OutputStream spool;
    private long         spoolPosition;
    private byte[]       copyBuffer;

    /**
     * @param spoolDirectory where values that cannot be referenced in place are spooled. It must
     *                       exist, and it should be readable only by this user: pixel data lands
     *                       in it.
     */
    public BufferedBulkDataCreator(final File spoolDirectory) {
        this.spoolDirectory = spoolDirectory;
    }

    @Override
    public BulkData createBulkData(final DicomInputStream in) throws IOException {
        return canReferenceIntoSource(in) ? referenceIntoSource(in) : spool(in);
    }

    /**
     * A reference into the source is only honest when the stream has one and reads it raw: a
     * Deflated dataset arrives through an inflater, so its positions are not file positions.
     */
    private static boolean canReferenceIntoSource(final DicomInputStream in) {
        return in.getURI() != null && !UID.DeflatedExplicitVRLittleEndian.equals(in.getTransferSyntax());
    }

    /**
     * What dcm4che does for a bulk data value when the stream has a URI -- record where the value
     * sits in the file and skip past it -- except that the reference reads ahead.
     */
    private static BulkData referenceIntoSource(final DicomInputStream in) throws IOException {
        final long length = in.unsignedLength();
        final BulkData reference = new ReadAheadBulkData(in.getURI(), in.getPosition(), length, in.bigEndian());
        in.skipFully(length);
        return reference;
    }

    /**
     * Copies the value at the stream position into the spool and returns a reference to it there.
     * Called for every bulk data value and every fragment of encapsulated pixel data.
     */
    private BulkData spool(final DicomInputStream in) throws IOException {
        final long length = in.unsignedLength();
        if (spool == null) {
            final File file = Files.createTempFile(spoolDirectory.toPath(), "mizer-bulk-", ".spool").toFile();
            spoolFiles.add(file);
            spoolFile     = file;
            spool         = new BufferedOutputStream(new FileOutputStream(file), DicomObjectFactory.BULK_DATA_BUFFER_SIZE);
            spoolPosition = 0;
        }
        if (copyBuffer == null) {
            copyBuffer = new byte[DicomObjectFactory.BULK_DATA_BUFFER_SIZE];
        }
        long remaining = length;
        while (remaining > 0) {
            final int read = in.read(copyBuffer, 0, (int) Math.min(copyBuffer.length, remaining));
            if (read < 0) {
                throw new EOFException("Stream ended " + remaining + " bytes short of a " + length
                                       + " byte value at " + in.getPosition());
            }
            spool.write(copyBuffer, 0, read);
            remaining -= read;
        }
        final BulkData reference = new ReadAheadBulkData(spoolFile.toURI().toString(), spoolPosition, length, in.bigEndian());
        spoolPosition += length;
        return reference;
    }

    /**
     * The files this creator spooled into: at most one, and none at all when every value could be
     * referenced in place or the read never reached bulk data.
     */
    public List<File> getSpoolFiles() {
        return new ArrayList<>(spoolFiles);
    }

    /**
     * Flushes and closes the spool, so that everything written to it can be read back through the
     * references. Idempotent.
     */
    @Override
    public void close() throws IOException {
        final OutputStream open = spool;
        if (open != null) {
            spool = null;
            open.close();
        }
    }
}
