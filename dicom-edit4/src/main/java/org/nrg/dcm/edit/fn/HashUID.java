/*
 * DicomEdit: org.nrg.dcm.edit.fn.HashUID
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit.fn;

import com.fasterxml.uuid.Generators;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.scripts.ScriptFunction;
import org.nrg.dicom.mizer.values.AbstractMizerValue;
import org.nrg.dicom.mizer.values.Value;
import org.nrg.dicom.mizer.variables.Variable;

import java.math.BigInteger;
import java.util.*;

/**
 * Creates a one-way hash UID by first creating a Version 5 UUID (SHA-1 hash) from the provided string,
 * then converting that UUID to a UID.
 * Portions of this code are derived from dcm4che, an MPL 1.1-licensed open source project
 * hosted at https://github.com/dcm4che/dcm4che.
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 * @author Gunter Zeilinger &lt;gunterze@gmail.com&gt; { original dcm4che implementation of UUID-to-UID conversion }
 */
public final class HashUID implements ScriptFunction {
    public static final String name = "hashUID";

    /**
     * UID root for UUIDs (Universally Unique Identifiers) generated as per Rec. ITU-T X.667 | ISO/IEC 9834-8.
     *
     * @see <a href="http://www.oid-info.com/get/2.25">OID repository {joint-iso-itu-t(2) uuid(25)}</a>
     */
    public static final String UUID_ROOT = "2.25";

    /* (non-Javadoc)
     * @see org.nrg.dcm.edit.ScriptFunction#apply(java.util.List)
     */
    public Value apply(final List<? extends Value> values) throws ScriptEvaluationException {
        if (values.isEmpty()) {
            throw new ScriptEvaluationException("usage: hashUID[string-to-hash {, optional algorithm-name} ]");
        }

        final Value value = values.getFirst();
        return new AbstractMizerValue() {
            public Set<Variable> getVariables() {
                return value.getVariables();
            }

            public SortedSet<Long> getTags() {
                return value.getTags();
            }

            public String on(final DicomObjectI dicomObject) throws ScriptEvaluationException {
                try {
                    final String extracted = value.on(dicomObject);
                    return extracted == null ? null : toUID(toUUID(extracted));
                } catch (Throwable t) {
                    throw new ScriptEvaluationException(t);
                }
            }

            public String on(Map<Integer, String> m) throws ScriptEvaluationException {
                try {
                    final String extracted = value.on(m);
                    return extracted == null ? null : toUID(toUUID(extracted));
                } catch (Throwable t) {
                    throw new ScriptEvaluationException(t);
                }
            }

            public void replace(final Variable variable) {
                value.replace(variable);
            }
        };
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
     * @param s source string
     *
     * @return Version 5 UUID
     */
    private static UUID toUUID(final String s) {
        return Generators.nameBasedGenerator().generate(s.getBytes());
    }
}
