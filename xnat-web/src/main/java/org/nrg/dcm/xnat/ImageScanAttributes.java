/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.ImageScanAttributes
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;


import org.dcm4che2.data.Tag;
import org.nrg.dcm.AttrDefs;
import org.nrg.dcm.Attributes;
import org.nrg.dcm.MutableAttrDefs;

import static org.nrg.dcm.Attributes.SeriesDate;
import static org.nrg.dcm.Attributes.SeriesTime;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
final class ImageScanAttributes {
    private ImageScanAttributes() {
    } // no instantiation

    static public AttrDefs get() {
        return ATTR_DEFS;
    }

    static final private MutableAttrDefs ATTR_DEFS = new MutableAttrDefs();

    static {
        ATTR_DEFS.add("ID");    // handled by session builder
        ATTR_DEFS.add("UID", Tag.SeriesInstanceUID);
        ATTR_DEFS.add("series_description", Tag.SeriesDescription);
        ATTR_DEFS.add("modality", Attributes.Modality);
        ATTR_DEFS.add("scanner", Tag.StationName);
        ATTR_DEFS.add("scanner/manufacturer", Tag.Manufacturer);
        ATTR_DEFS.add("scanner/model", Tag.ManufacturerModelName);
        ATTR_DEFS.add(new XnatAttrDef.Time("startTime", SeriesTime));
        ATTR_DEFS.add(new XnatAttrDef.Date("start_date", SeriesDate));
        ATTR_DEFS.add("requestedProcedureDescription", Tag.RequestedProcedureDescription);
        ATTR_DEFS.add("protocolName", Tag.ProtocolName);
        ATTR_DEFS.add("bodyPartExamined", Tag.BodyPartExamined);
    }
}
