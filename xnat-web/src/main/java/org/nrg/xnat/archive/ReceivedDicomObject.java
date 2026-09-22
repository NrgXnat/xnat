/*
 * web: org.nrg.xnat.archive.ReceivedDicomObject
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.nrg.dcm.io.ResumableDicomInputStream;
import org.nrg.dicom.mizer.objects.BufferedBulkDataCreator;
import org.nrg.dicom.mizer.objects.Dcm4cheConvert;
import org.nrg.dicom.mizer.objects.DicomObjectWriter;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Predicate;

/**
 * A DICOM object arriving on an import stream, read once and written once.
 * <p>
 * The importer used to read only the header it needed to identify the object, write that header
 * followed by the rest of the stream byte for byte, and then, if a script applied, read the file
 * back, anonymize it and write it again. Reading the whole object up front keeps the pixel data off
 * the heap -- dcm4che spools it to scratch and the dataset holds references, see
 * {@link ResumableDicomInputStream#openWithBulkDataOffHeap} -- so scripts can run against the
 * dataset in memory and the object can be written to its final place exactly once, with the pixel
 * data streamed from the spool. The archive never holds a version the scripts have not seen.
 * <p>
 * The partial read is kept for imports no script applies to. Those never had a second pass, so a
 * spool would be pure cost, and they are written exactly as they always were.
 * <p>
 * Kept apart from {@link GradualDicomImporter} because any static touch of that class runs
 * {@code ImporterHandlerA}'s initializer, which needs a Spring context. This class can be exercised
 * on its own.
 */
@Slf4j
final class ReceivedDicomObject implements Closeable {
    /** Read every element to the end of the stream. */
    private static final Predicate<DicomInputStream> WHOLE_OBJECT = stream -> false;

    /**
     * Reads the file meta information and either the whole dataset or the header through
     * <b>lastTag</b>, leaving the stream positioned at the first element not read.
     *
     * @param source                   the object's bytes.
     * @param transferSyntaxFromCaller the transfer syntax the caller negotiated, or null to take the
     *                                 stream's own.
     * @param lastTag                  for a partial read, the last tag the caller needs. Ignored for
     *                                 a whole read.
     * @param whole                    whether to read the whole object. A Deflated source is read whole
     *                                 regardless, because its dataset cannot be split across a partial
     *                                 read and a raw copy of the remainder.
     */
    static ReceivedDicomObject read(final InputStream source, final String transferSyntaxFromCaller,
                                    final int lastTag, final boolean whole) throws IOException {
        return read(source, transferSyntaxFromCaller, lastTag, whole, null);
    }

    /**
     * As {@link #read(InputStream, String, int, boolean)}, for a source that is a file the stream
     * reads from its first byte: a whole read then references pixel data straight into the file
     * instead of copying it to the spool -- the inbox hands the importer exactly such sources --
     * except under a Deflated transfer syntax, where the spool is still used because file offsets
     * would be dishonest. The file must outlive {@link #write}; nothing here alters or deletes it.
     *
     * @param sourceFile the file the stream reads, or null when the source is not a file.
     */
    static ReceivedDicomObject read(final InputStream source, final String transferSyntaxFromCaller,
                                    final int lastTag, final boolean whole, final File sourceFile) throws IOException {
        final BufferedInputStream       in  = new BufferedInputStream(source);
        final ResumableDicomInputStream dis = ResumableDicomInputStream.openWithBulkDataOffHeap(in, sourceFile);
        try {
            Attributes fmi = dis.readFileMetaInformation();
            final String     transferSyntax = null == transferSyntaxFromCaller ? dis.getTransferSyntax() : transferSyntaxFromCaller;
            // A deflated source -- Deflated Explicit VR LE or either JPIP Referenced Deflate syntax,
            // the three dcm4che inflates -- is one continuous zlib stream past the file meta group.
            // The partial read stops mid-stream and lets write() copy the raw remainder through, but
            // a fresh header followed by the leftover compressed bytes does not re-read -- the object
            // comes back malformed. So a deflated object is always read whole, whether or not a
            // script applies; write() then re-deflates the parsed dataset into a valid object.
            final boolean    readWhole      = whole || BufferedBulkDataCreator.isDeflated(transferSyntax);
            final Attributes dataset        = new Attributes();
            if (readWhole) {
                dis.readAttributes(dataset, -1, WHOLE_OBJECT);
            } else {
                // The last tag the caller needs, not a stop tag: dcm4che's stop tag is exclusive, so
                // the read adds the one.
                dis.readAttributes(dataset, -1, lastTag + 1);
                // Back to the header of the element the read stopped at, so write() can copy the
                // rest of the stream through untouched.
                dis.reset();
            }
            // CStore (DIMSE) has no FMI preamble, so the meta group is synthesized from the
            // dataset; either way it is merged in so processors see a complete DICOM object, and
            // split back out at write time.
            Dcm4cheConvert.mergeFileMetaInformation(dataset, fmi, transferSyntax);
            return new ReceivedDicomObject(in, dis, dataset, transferSyntax, readWhole);
        } catch (IOException | RuntimeException e) {
            // Nothing is going to own the spool files if the read fails.
            discard(dis);
            throw e;
        }
    }

    private ReceivedDicomObject(final BufferedInputStream in, final ResumableDicomInputStream dis,
                                final Attributes dataset, final String transferSyntax, final boolean whole) {
        _in             = in;
        _dis            = dis;
        _dataset        = dataset;
        _transferSyntax = transferSyntax;
        _whole          = whole;
    }

    /** The dataset with the file meta information merged in, as processors and scripts expect it. */
    Attributes getDataset() {
        return _dataset;
    }

    String getTransferSyntax() {
        return _transferSyntax;
    }

    /**
     * True when the whole object was read: the pixel data is referenced from a spool file and
     * nothing remains on the stream.
     */
    boolean isWhole() {
        return _whole;
    }

    /**
     * Writes the object: the file meta information, the dataset, and then, after a partial read, the
     * rest of the stream byte for byte. Bulk data the dataset references is streamed from wherever
     * it sits.
     *
     * @param dataset       the dataset to write, with the file meta information merged in. Passed
     *                      rather than taken from this object because a processor may have handed
     *                      back a different instance.
     * @param sourceAeTitle recorded in the file meta information when present.
     * @param outputFile    where to write.
     * @param source        who sent the object, for the receipt log.
     */
    void write(final Attributes dataset, final Object sourceAeTitle, final File outputFile, final String source) throws IOException {
        try (final FileOutputStream fos = new FileOutputStream(outputFile)) {
            // After a partial read the rest of the stream, pixel data included, has not been
            // parsed and is copied through as it arrived.
            final long copied = DicomObjectWriter.write(dataset, fos,
                    fmi -> {
                        if (null != sourceAeTitle) {
                            fmi.setString(Tag.SourceApplicationEntityTitle, VR.AE, (String) sourceAeTitle);
                        }
                    },
                    _whole ? null : _in);
            if (!_whole) {
                log.trace("copied {} additional bytes to {}", copied, outputFile);
            }
        }
        LoggerFactory.getLogger("org.nrg.xnat.received").info("{}:{}", source, outputFile);
    }

    /**
     * Closes the stream and deletes anything spooled from it. Only safe once the object has been
     * written: until then the dataset holds references into the spool files.
     */
    @Override
    public void close() throws IOException {
        discard(_dis);
    }

    private static void discard(final ResumableDicomInputStream dis) throws IOException {
        try {
            dis.close();
        } finally {
            ResumableDicomInputStream.deleteBulkDataFiles(dis.getSpoolFiles());
        }
    }

    private final BufferedInputStream       _in;
    private final ResumableDicomInputStream _dis;
    private final Attributes                _dataset;
    private final String                    _transferSyntax;
    private final boolean                   _whole;
}
