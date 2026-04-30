/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.XRayTubeCurrentAttribute
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.xnat;

import java.util.Arrays;
import java.util.Map;

import org.dcm4che3.data.Tag;
import org.nrg.attr.ConversionFailureException;
import org.nrg.attr.ExtAttrException;
import org.nrg.attr.ExtAttrValue;
import org.nrg.attr.NoUniqueValueException;
import org.nrg.dcm.DicomAttributeIndex;
import org.nrg.dcm.DicomAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
class XRayTubeCurrentAttribute extends XnatAttrDef.Abstract<Double> {
    private final Logger logger = LoggerFactory.getLogger(XRayTubeCurrentAttribute.class);

    public XRayTubeCurrentAttribute(final String name, final String modality, int functionalSequenceTag) {
        super(name,
                DicomAttributes.chain(modality + "_XRayTubeCurrent", Tag.XRayTubeCurrent, functionalSequenceTag),
                DicomAttributes.chain(modality + "_XRayTubeCurrent_mA", Tag.XRayTubeCurrentInmA, functionalSequenceTag),
                DicomAttributes.chain(modality + "_XRayTubeCurrent_uA", Tag.XRayTubeCurrentInuA, functionalSequenceTag));
        for (final DicomAttributeIndex dai : getAttrs()) {
            makeOptional(dai);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.AbstractExtAttrDef#apply(java.lang.Double)
     */
    public Iterable<ExtAttrValue> apply(final Double a) throws ExtAttrException {
        return applyDouble(a);
    }
    
    /*
     * (non-Javadoc)
     * @see org.nrg.attr.EvaluableAttrDef#foldl(java.lang.Object, java.util.Map)
     */
    public Double foldl(final Double a, final Map<? extends DicomAttributeIndex,? extends String> m)
            throws ExtAttrException {
        Double dv = null;
        ConversionFailureException cfe = null;
        for (final Map.Entry<? extends DicomAttributeIndex,? extends String> me : m.entrySet()) {
            final String v = me.getValue();
            if (!Strings.isNullOrEmpty(v)) {
                try {
                    final String name = me.getKey().getAttributeName(null);
                    if (name.endsWith("Current") || name.endsWith("Current_mA")) {
                        dv = Double.parseDouble(v);
                    } else if (name.endsWith("Current_uA")) {
                        dv = Double.parseDouble(v)/1000.0;
                    }
                    break;
                } catch (NumberFormatException e) {
                    logger.debug("couldn't parse x-ray tube current from " + v, e);
                    cfe = new ConversionFailureException(me.getKey(), v, "not a valid number");
                }
            }
        }
        if (null == dv) {
            if (null == cfe) {
                return a;
            } else {
                throw cfe;
            }
        } else if (null == a || a.equals(dv)) {
            return dv;
        } else {
            throw new NoUniqueValueException(getName(), Arrays.asList(a, dv));
        }
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.Foldable#start()
     */
    public Double start() { return null; }
}
