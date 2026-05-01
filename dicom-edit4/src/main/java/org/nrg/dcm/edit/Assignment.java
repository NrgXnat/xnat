/*
 * DicomEdit: org.nrg.dcm.edit.Assignment
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.VR;
import org.dcm4che2.util.TagUtils;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;
import org.nrg.dicom.mizer.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class Assignment extends AbstractOperation {
    private final Logger logger = LoggerFactory.getLogger(Assignment.class);
    private final TagPattern pattern;
    private final Value      value;

    public Assignment(final TagPattern pattern, final Value value) {
        super("Assign");
        this.pattern = pattern;
        this.value = value;
    }

    public Assignment(final int tag, final Value value) {
        this(new TagPattern(tag), value);
    }

    public Assignment(final Integer tag, final Value value) {
        this(tag.intValue(), value);
    }

    public Assignment(final int tag, final String value) {
        this(tag, new ConstantValue(value));
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Operation#affects(int)
     */
    public boolean affects(final int tag) {
        return pattern.apply(tag);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Operation#getTopTag()
     */
    public long getTopTag() {
        return getScriptTags().last();
    }

    @Override
    public SortedSet<Long> getScriptTags() {
        final SortedSet<Long> tags = (value != null && value.getTags() != null) ? Sets.newTreeSet(value.getTags()) : Sets.<Long>newTreeSet();
        tags.add(pattern.getTopTag());
        return tags;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (o instanceof Assignment) {
            final Assignment oa = (Assignment) o;
            return pattern.equals(oa.pattern) && value.equals(oa.value);
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return Objects.hashCode(pattern, value);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Operation#makeAction(org.dcm4che2.data.DicomObject)
     */
    public Action makeAction(final DicomObjectI dicomObject) throws ScriptEvaluationException {
        final String value = this.value.on(dicomObject);
        return new Action() {
            public void apply() throws ScriptEvaluationException {
                final DicomObject    dcm4che2Object = dicomObject.getDcm4che2Object();
                final Iterable<Long> iterable       = pattern.apply(dcm4che2Object);
                if (iterable != null) {
                    for (final long longTag : iterable) {
                        final int tag = (int) longTag;
                        VR vr = dcm4che2Object.vrOf(tag);
                        if (VR.UN.equals(vr)) {
                            logger.debug("VR for {} is UN; writing as LO instead", TagUtils.toString(tag));
                            vr = VR.LO;
                        }
                        try {
                            dcm4che2Object.putString(new int[]{tag}, vr, value);
                        } catch (UnsupportedOperationException e) {
                            throw new ScriptEvaluationException("Unable to set attribute "
                                                                + TagUtils.toString(tag) + " (VR " + vr + ") to \"" + value + "\"", e);
                        }
                    }
                }
            }

            public String toString() {
                return MessageFormat.format("{0}: {1} := {2}", getName(), pattern, value);
            }
        };
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Operation#replace(org.nrg.dcm.edit.Variable)
     */
    public void replace(final Variable v) {
        value.replace(v);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Operation#apply(java.util.Map)
     */
    public String apply(final Map<Integer, String> values) throws ScriptEvaluationException {
        return value.on(values);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuilder buffer = new StringBuilder(getName());
        buffer.append(" ");
        buffer.append(pattern);
        String extracted;
        try {
            extracted = value.on(new HashMap<Integer, String>());
        } catch (ScriptEvaluationException e) {
            extracted = value.toString();
        }
        buffer.append(" := ").append(extracted);
        return buffer.toString();
    }
}
