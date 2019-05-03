/*
 * web: org.nrg.xnat.actions.postArchive.ClearStudyRoutingAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.actions.postArchive;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.PrearcSessionArchiver;
import org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtils;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * ClearStudyRemappingAction class.
 */
@SuppressWarnings("unused")
@Slf4j
public class ClearStudyRemappingAction implements PrearcSessionArchiver.PostArchiveAction {
    @Override
    public Boolean execute(final UserI user, final @Nullable XnatImagesessiondata src, final Map<String, Object> params) {
        final String studyInstanceUid = src != null ? src.getUid() : (String) params.get("studyInstanceUid");
        if (StringUtils.isNotBlank(studyInstanceUid)) {
            try {
                final String script = DefaultAnonUtils.getService().getStudyScript(studyInstanceUid);

                if (StringUtils.isNotBlank(script)) {
                    final UserI adminUser = Users.getAdminUser();
                    DefaultAnonUtils.getService().disableStudy(adminUser != null ? adminUser.getUsername() : "admin", studyInstanceUid);
                    return true;
                }
            } catch (Exception e) {
                log.error("Error when clearing study remapping information.", e);
            }
        }
        return false;
    }
}
