package org.nrg.dcm.io;

import org.dcm4che3.io.DicomInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.function.Predicate;

/**
 * DicomInputStream that calls mark() before reading each header, so that if a stop predicate
 * stops the read, calling reset() makes it possible to resume reading.
 */
public final class ResumableDicomInputStream extends DicomInputStream {
    // 12 should be enough, but more might be needed someday to not interfere with readSequence.
    final int markSize = 12;

    public ResumableDicomInputStream(BufferedInputStream in) throws IOException {
        super(in);
    }

    public ResumableDicomInputStream(BufferedInputStream in, String transferSyntaxUid) throws IOException {
        super(in, transferSyntaxUid);
    }

    @Override
    public void readHeader(final Predicate<DicomInputStream> stopPredicate) throws IOException {
        mark(markSize);
        super.readHeader(stopPredicate);
    }
}
