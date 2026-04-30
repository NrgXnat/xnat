/*
 * dicom-xnat-util: org.nrg.xnat.Attributesdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xnat;

import org.dcm4che3.data.Attributes;

import java.util.SortedSet;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public interface DicomObjectIdentifier<ProjectT> {
    /**
     * Determines to which project a specified DICOM object belongs
     * @param o DicomObject
     * @return project object
     */
    ProjectT getProject(Attributes o);
    
    String getSessionLabel(Attributes o);
    
    String getSubjectLabel(Attributes o);

    /**
     * What DICOM attributes does this identifier use?
     * @return sorted set of DICOM attribute tags
     */
    SortedSet<Integer> getTags();
    
    /**
     * Does this object request autoarchiving?
     * @param o DicomObject
     * @return true if object requests autoarchiving
     * @return false if object requests no autoarchiving
     * @return null if object does not specify autoarchiving
     */
    Boolean requestsAutoarchive(Attributes o);

    /**
     * Does this object support processing per-receiver routing expressions?
     * @return true if supported.
     */
    default boolean isCustomRoutingSupported() {
        return false;
    }
}
