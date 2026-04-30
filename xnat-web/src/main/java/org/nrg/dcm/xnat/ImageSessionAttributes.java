/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.ImageSessionAttributes
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import org.dcm4che3.data.Tag;
import org.nrg.attr.LabeledAttrDef;
import org.nrg.attr.ValueJoiningAttrDef;
import org.nrg.dcm.AttrDefs;
import org.nrg.dcm.MutableAttrDefs;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
class ImageSessionAttributes {
    private ImageSessionAttributes() {}    // no instantiation
    static public AttrDefs get() { return s; }

    static final private MutableAttrDefs s = new MutableAttrDefs();
    static {
        // xnat:experimentData
        s.add(new XnatAttrDef.Date("date", Tag.StudyDate));
        s.add(new XnatAttrDef.Time("time", Tag.StudyTime));
        s.add("acquisition_site", Tag.InstitutionName);

        s.add(LabeledAttrDef.create(new XnatAttrDef.Text("fields/field", Tag.StudyComments),
                "name", "studyComments"));

        // xnat:imageSessionData
        s.add("UID", Tag.StudyInstanceUID);
        s.add("session_type", Tag.StudyDescription);
        s.add("modality", Tag.Modality);

        s.add(ValueJoiningAttrDef.wrap(new XnatAttrDef.Text("scanner", Tag.StationName), ","));
        s.add("scanner/manufacturer", Tag.Manufacturer);
        s.add("scanner/model", Tag.ManufacturerModelName);

        s.add("operator", Tag.OperatorsName);
        s.add("prearchivePath");

        s.add("dcmAccessionNumber", Tag.AccessionNumber);
        s.add("study_id", Tag.StudyID);
        s.add("dcmPatientId", Tag.PatientID);
        s.add("dcmPatientName", Tag.PatientName);
        s.add(new XnatAttrDef.Date("dcmPatientBirthDate", Tag.PatientBirthDate));
        s.add("dcmPatientWeight",Tag.PatientWeight);
    }
}
