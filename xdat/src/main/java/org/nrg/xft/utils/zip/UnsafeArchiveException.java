/*
 * core: org.nrg.xft.utils.zip.UnsafeArchiveException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.utils.zip;

import java.io.IOException;

/**
 * Thrown when an archive (zip, tar, or tgz) upload is rejected because it contains one or more entries whose
 * relative path would resolve outside of the intended extraction destination -- a path traversal / "zip-slip"
 * attempt.
 *
 * <p>This is deliberately a distinct type from the plain {@link IOException} extraction otherwise throws on a
 * genuine I/O failure. Code that generically catches {@code IOException} around an extraction call -- for example
 * to fall back to copying the archive as-is when extraction fails -- must check for this type first and let it
 * propagate rather than swallowing it into that kind of fallback. Treating a rejection the same as an ordinary
 * extraction failure would silently defeat the protection this exception exists to enforce: the archive would end
 * up copied, unextracted but still fully intact, into wherever the fallback writes it, and the caller (and any
 * client waiting on the request) would see success instead of the rejection.</p>
 */
public class UnsafeArchiveException extends IOException {
    public UnsafeArchiveException(final String message) {
        super(message);
    }
}
