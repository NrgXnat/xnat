/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.LabelAttrDef
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import org.nrg.attr.BasicExtAttrValue;
import org.nrg.attr.EvaluableAttrDef;
import org.nrg.attr.ExtAttrValue;
import org.nrg.attr.TransformingExtAttrDef;
import org.nrg.dcm.DicomAttributeIndex;
import org.nrg.xnat.Labels;

import com.google.common.base.Function;

/**
 * Changes contained attribute value to a valid XNAT label.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class LabelAttrDef<A> extends TransformingExtAttrDef<DicomAttributeIndex,String,A> implements XnatAttrDef {
    public LabelAttrDef(final EvaluableAttrDef<DicomAttributeIndex,String,A> underlying) {
        super(underlying, new Function<ExtAttrValue,ExtAttrValue>() {
            public ExtAttrValue apply(final ExtAttrValue v) {
                return new BasicExtAttrValue(v.getName(), Labels.toLabelChars(v.getText()), v.getAttrs());
            }
        });
    }

    public static <A> LabelAttrDef<A> wrap(final EvaluableAttrDef<DicomAttributeIndex,String,A> underlying) {
        return new LabelAttrDef<A>(underlying);
    }

    @SuppressWarnings("unchecked")
    public static LabelAttrDef<?> wrap(final XnatAttrDef underlying) {
        return wrap((EvaluableAttrDef<DicomAttributeIndex,String,?>)underlying);
    }
}
