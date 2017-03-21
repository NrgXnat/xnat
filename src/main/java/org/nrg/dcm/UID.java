/*
 * dicomtools: org.nrg.dcm.UID
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * DICOM Unique Identifier (UID)
 * See DICOM standard, PS 3.5 Section 9
 * (also PS 3.5 Annex C for examples)
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class UID implements Serializable, Comparable<UID> {
    private static final long serialVersionUID = 1L;

    private static final int     MAX_LEN = 64;
    private static final Pattern P       = Pattern.compile("([1-9][0-9]*)?[0-9](\\.([1-9][0-9]*)?[0-9])+");

    private final String _uid;

    public static final class InvalidUIDException extends Exception {
        private static final long serialVersionUID = 1L;

        private InvalidUIDException(final String uid) {
            super(uid + " is not a valid UID");
        }
    }

    /**
     * Creates a UID object from the given UID String
     *
     * @param uid UID as a String
     *
     * @throws InvalidUIDException if the argument is not a valid UID
     */
    public UID(final String uid) throws InvalidUIDException {
        check(_uid = uid);
    }

    /**
     * Verify that this is a valid UID
     *
     * @param uid candidate UID
     *
     * @throws InvalidUIDException if the candidate UID is invalid
     */
    private void check(final String uid) throws InvalidUIDException {
        if (null == uid || !P.matcher(uid).matches() || uid.length() > MAX_LEN) {
            throw new InvalidUIDException(uid);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(@Nonnull final UID uid) {
        return _uid.compareTo(uid._uid);
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object object) {
        return object != null && object instanceof UID && _uid.equals(((UID) object)._uid);
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return _uid.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return _uid;
    }
}
