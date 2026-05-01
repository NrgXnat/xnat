/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.PETSessionAttributes
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import org.nrg.dcm.AttrDefs;
import org.nrg.dcm.MutableAttrDefs;

import static org.nrg.dcm.DicomAttributes.*;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
final class PETSessionAttributes {
    private PETSessionAttributes() {}    // no instantiation
    static public AttrDefs get() { return s; }

    static final private MutableAttrDefs s = new MutableAttrDefs(ImageSessionAttributes.get());

    static {
        s.add("tracer/name", PET_RADIOPHARMACEUTICAL);
        s.add(new XnatAttrDef.Date("tracer/startTime", PET_RADIOPHARMACEUTICAL_STARTDATETIME));
        s.add("tracer/dose", PET_RADIOPHARMACEUTICAL_DOSE);
        s.add("tracer/dose/units", "Bq");
        s.add("tracer/isotope/half-life", PET_RADIONUCLIDE_HALFLIFE);
    }
}
