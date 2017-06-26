/*
 * web: org.nrg.dcm.id.RoutedStudyDicomProjectIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.id;

import com.google.common.collect.ImmutableSortedSet;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.services.StudyRoutingService;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.SortedSet;

@Service
public final class RoutedStudyDicomProjectIdentifier implements DicomProjectIdentifier {
    @Autowired
    public RoutedStudyDicomProjectIdentifier(final StudyRoutingService service) {
        _service = service;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Integer> getTags() { return tags; }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatProjectdata apply(final UserI user, final DicomObject dicom) {
        final String studyInstanceUid = dicom.getString(Tag.StudyInstanceUID);
        if (!StringUtils.isBlank(studyInstanceUid)) {
            Map<String, String> routing = _service.findStudyRouting(studyInstanceUid);
            if (routing != null) {
                final XnatProjectdata project = XnatProjectdata.getProjectByIDorAlias(routing.get(StudyRoutingService.PROJECT), user, false);
                if (project != null) {
                    if (_log.isDebugEnabled()) {
                        _log.debug("Found project assignment of " + project.getProject() + " for study instance UID " + studyInstanceUid);
                    }
                    return project;
                } else {
                    throw new RuntimeException("The study instance UID " + studyInstanceUid + " has a routing assignment for the project ID " + routing.get(StudyRoutingService.PROJECT) + ", but I couldn't find a project with that ID.");
                }
            } else if (_log.isDebugEnabled()) {
                _log.debug("Found no project routing assignment for study instance UID " + studyInstanceUid);
            }
        } else if (_log.isWarnEnabled()) {
            _log.warn("No study instance UID found for DICOM object! That's probably not good.");
        }

        return null;
    }

    @Override
    public void reset() {
        _service.closeAll();
    }

    private static final Logger _log = LoggerFactory.getLogger(RoutedStudyDicomProjectIdentifier.class);

    private static final ImmutableSortedSet<Integer> tags = ImmutableSortedSet.of();

    private final StudyRoutingService _service;
}
