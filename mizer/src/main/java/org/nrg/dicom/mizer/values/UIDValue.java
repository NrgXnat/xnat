/*
 * mizer: org.nrg.dicom.mizer.values.UIDValue
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.values;

import com.fasterxml.uuid.Generators;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Creates a UID value.
 */
public class UIDValue extends MizerValueShim {
    public UIDValue(final String value) {
        super(new ConstantValue(value));
    }

    public UIDValue(final Value value) {
        super(value);
    }

    public UIDValue hashUIDValue() throws ScriptEvaluationException {
        return getHashedUIDValue(getValue());
    }

    public static UIDValue getHashedUIDValue(final Value value) throws ScriptEvaluationException {
        return value == null ? null : getHashedUIDValue(value.asString());
    }

    public static UIDValue getHashedUIDValue(final String value) throws ScriptEvaluationException {
        final String hashed = getHashedUID(value);
        return new UIDValue(hashed);
    }

    public static String getHashedUID(final String value) {
        return null == value ? null : toUID(toUUID(value));
    }

    private static String toUID(final UUID uuid) {
        final byte[] b17 = new byte[17];
        fill(b17, 1, uuid.getMostSignificantBits());
        fill(b17, 9, uuid.getLeastSignificantBits());
        return UUID_ROOT + '.' + new BigInteger(b17);
    }

    private static void fill(byte[] bb, int off, long val) {
        for (int i = off, shift = 56; shift >= 0; i++, shift -= 8) {
            bb[i] = (byte) (val >>> shift);
        }
    }

    /**
     * Generates a Version 5 UUID from the provided string
     *
     * @param string source string
     *
     * @return Version 5 UUID
     */
    private static UUID toUUID(final String string) {
        return Generators.nameBasedGenerator().generate(string.getBytes());
    }

    /**
     * UID root for UUIDs (Universally Unique Identifiers) generated as per Rec. ITU-T X.667 | ISO/IEC 9834-8.
     *
     * @see <a href="http://www.oid-info.com/get/2.25">OID repository {joint-iso-itu-t(2) uuid(25)}</a>
     */
    private static final String UUID_ROOT = "2.25";
}
