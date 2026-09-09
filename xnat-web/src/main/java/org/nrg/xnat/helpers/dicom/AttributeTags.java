/*
 * web: org.nrg.xnat.helpers.dicom.AttributeTags
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.dicom;

import org.dcm4che3.util.TagUtils;

/**
 * Resolves the attribute tags named in a {@code ?field=} parameter.
 */
public final class AttributeTags {
    /**
     * What {@link #forName} returns for text it cannot resolve, and what {@link TagUtils#forName} returns for
     * the same. dcm4che does not name it. It is safe as a sentinel because DICOM PS3.5 reserves group FFFF, so
     * (FFFF,FFFF) cannot legally appear in an object.
     */
    public static final int NOT_RESOLVED = -1;

    private AttributeTags() {
    }

    /**
     * Resolves a keyword or a bare hexadecimal tag.
     * <p>
     * {@link TagUtils#forName} reads hex with a signed parse, so it reports anything above 0x7FFFFFFF as
     * unresolved rather than throwing -- that is every tag in a private group &gt;= 0x8000. Reading it again
     * unsigned recovers those.
     *
     * @param name a keyword such as PatientName, or eight hexadecimal digits.
     *
     * @return the tag, or {@link #NOT_RESOLVED}.
     */
    public static int forName(final String name) {
        final int tag = TagUtils.forName(name);
        if (NOT_RESOLVED != tag) {
            return tag;
        }
        try {
            return Integer.parseUnsignedInt(name, 16);
        } catch (NumberFormatException e) {
            return NOT_RESOLVED;
        }
    }
}
