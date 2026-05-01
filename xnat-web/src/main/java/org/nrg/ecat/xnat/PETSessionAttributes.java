/*
 * ecat4xnat: org.nrg.ecat.xnat.PETSessionAttributes
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.ecat.xnat;

import org.nrg.attr.AttrDefs;
import org.nrg.ecat.Variable;
import static org.nrg.ecat.Variable.*;
import org.nrg.ecat.AttrDefSet;

final class PETSessionAttributes {
	private PETSessionAttributes() {}     // no instantiation

	static public AttrDefs<Variable> get() { return s; }

	static final private AttrDefSet s = new AttrDefSet();
	static {
		// xnat: experimentData
		s.add(new XnatAttrDef.Word("label", PATIENT_ID));
		s.add("date");      // need earliest date: handled explicitly
		s.add("time");      // need earliest time: handled explicitly

		// xnat:subjectAssessorData
		s.add(new XnatAttrDef.Word("subject_ID", PATIENT_NAME));

		// xnat:imageSessionData
		s.add(new XnatAttrDef.Constant("modality", "PET"));

		// xnat:petSessionData:
		s.add("operator", OPERATOR_NAME);
		s.add("prearchivePath");    // handled explicitly
		s.add("studyType", STUDY_TYPE);
		s.add("patientID", PATIENT_ID);
		s.add("patientName", PATIENT_NAME);

		s.add("tracer");	// hierarchical and complicated: handled explicitly
	}
}
