/*
 * web: org.nrg.xnat.actions.postArchive.ClearStudyRoutingAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.actions.postArchive;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.services.StudyRoutingService;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.PrearcSessionArchiver;

/**
 * ClearStudyRoutingAction class.
 */
@SuppressWarnings("unused")
public class ClearStudyRoutingAction implements PrearcSessionArchiver.PostArchiveAction {
    @Override
    public Boolean execute(final UserI user, final XnatImagesessiondata src, final Map<String, Object> params) {
        final String studyInstanceUid = src.getUid();
        return StringUtils.isNotBlank(studyInstanceUid) && XDAT.getContextService().getBean(StudyRoutingService.class).close(studyInstanceUid);
    }
}
