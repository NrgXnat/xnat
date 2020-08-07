package org.nrg.xnat.eventservice.services;

import org.nrg.xdat.model.XnatExperimentdataI;
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.model.XnatSubjectassessordataI;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xdat.om.XnatExperimentdata;

import java.util.List;

public interface XnatObjectIntrospectionService {

    Boolean isModified(XnatExperimentdataI experiment);

    Boolean hasResource(XnatExperimentdataI experiment);

    Boolean hasHistory(XnatSubjectdataI subject);

    Boolean hasResource(XnatSubjectdataI subject);

    Boolean storedInDatabase(XnatSubjectdataI subject);

    List<String> getStoredImageSessionIds(XnatSubjectdataI subject);

    List<String> getStoredSubjectAssessorIds(XnatSubjectdataI subject);

    List<String> getStoredNonImageSubjectAssessorIds(XnatSubjectdataI subject);

    Boolean storedInDatabase(XnatExperimentdata experiment);

    List<String> getStoredScanIds(XnatExperimentdata experiment);

    boolean storedInDatabase(XnatImageassessordataI assessor);

    boolean storedInDatabase(XnatSubjectassessordataI subjectassessor);

    Integer getResourceCount(XnatProjectdataI project);

}
