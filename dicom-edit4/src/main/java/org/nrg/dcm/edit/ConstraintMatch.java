/*
 * DicomEdit: org.nrg.dcm.edit.ConstraintMatch
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.util.SortedSet;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
interface ConstraintMatch {
    SortedSet<Long> getTags();
    boolean matches(DicomObjectI o) throws ScriptEvaluationException;
}
