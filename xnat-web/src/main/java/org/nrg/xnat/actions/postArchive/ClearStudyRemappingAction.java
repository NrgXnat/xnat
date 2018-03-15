/*
 * web: org.nrg.xnat.actions.postArchive.ClearStudyRoutingAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.actions.postArchive;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.services.StudyRoutingService;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.PrearcSessionArchiver;
import org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * ClearStudyRemappingAction class.
 */
@SuppressWarnings("unused")
public class ClearStudyRemappingAction implements PrearcSessionArchiver.PostArchiveAction {

    private static final Logger _log = LoggerFactory.getLogger(ClearStudyRemappingAction.class);

    @Override
    public Boolean execute(final UserI user, final XnatImagesessiondata src, final Map<String, Object> params) {
        final String studyInstanceUid = src.getUid();
        if(StringUtils.isNotBlank(studyInstanceUid)){
            try {
                String script = DefaultAnonUtils.getService().getStudyScript(studyInstanceUid);

                if (StringUtils.isNotBlank(script)) {
                    DefaultAnonUtils.getService().disableStudy(AdminUtils.getAdminUser().getLogin(), studyInstanceUid);
                    return true;
                }
            }
            catch(Exception e){
                _log.error("Error when clearing study remapping information.",e);
            }
        }
        return false;
    }
}
