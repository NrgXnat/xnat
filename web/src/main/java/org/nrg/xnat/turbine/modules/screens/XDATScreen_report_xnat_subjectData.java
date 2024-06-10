/*
 * web: org.nrg.xnat.turbine.modules.screens.XDATScreen_report_xnat_subjectData
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.XnatProjectparticipantI;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.modules.screens.SecureReport;
import org.nrg.xdat.turbine.modules.screens.XdatScreen;
import org.nrg.xft.security.UserI;

/**
 * @author Tim
 */
@XdatScreen
@Slf4j
public class XDATScreen_report_xnat_subjectData extends SecureReport {
    /**
     * {@inheritDoc}
     */
    public void finalProcessing(final RunData data, final Context context) {
        final UserI user = XDAT.getUserDetails();
        assert user != null;

        final XnatSubjectdata subject = new XnatSubjectdata(item);
        context.put("subject", subject);

        if (context.get("project") == null) {
            final String project = getReadableProject(subject, user);
            if (StringUtils.isNotBlank(project)) {
                context.put("project", project);
            } else {
                log.info("No readable project for the subject '{}' was found for the user '{}'", subject.getId(), user.getUsername());
                context.put("project", "");
            }
        }
    }

    private String getReadableProject(final XnatSubjectdata subject, final UserI user) {
        final String project = subject.getProject();
        if (Permissions.canReadProject(user, project)) {
            return project;
        }
        final Optional<XnatProjectparticipantI> shared = Iterables.tryFind(subject.getSharing_share(), new Predicate<XnatProjectparticipantI>() {
            @Override
            public boolean apply(final XnatProjectparticipantI shared) {
                return Permissions.canReadProject(user, shared.getProject());
            }
        });
        return shared.isPresent() ? shared.get().getProject() : null;
    }
}
