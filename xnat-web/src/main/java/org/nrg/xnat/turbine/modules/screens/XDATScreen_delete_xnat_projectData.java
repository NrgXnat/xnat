/*
 * web: org.nrg.xnat.turbine.modules.screens.XDATScreen_delete_xnat_projectData
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.action.ClientException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.XnatSubjectassessordataI;
import org.nrg.xdat.om.XnatAbstractprojectasset;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.turbine.modules.screens.SecureReport;
import org.nrg.xft.security.UserI;
import org.restlet.data.Status;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Slf4j
public class XDATScreen_delete_xnat_projectData extends SecureReport {
    @Override
    public void finalProcessing(final RunData data, final Context context) {
        final UserI           user    = XDAT.getUserDetails();
        final XnatProjectdata project = (XnatProjectdata) context.get("om");
        if (project == null) {
            throw new NrgServiceRuntimeException(new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "You must provide a project to be deleted"));
        }
        final String projectId = project.getId();

        final List<XnatAbstractprojectasset> projectAssets = XnatAbstractprojectasset.getXnatAbstractprojectassetsByField(XnatAbstractprojectasset.SCHEMA_ELEMENT_NAME + "/project", projectId, user, false);
        log.debug("Found {} assets for project {}", projectAssets.size(), projectId);
        context.put("projectAssets", projectAssets);

        final Map<XnatSubjectdata, Map<XnatSubjectassessordataI, List<?>>> projectData;
        if (!project.getParticipants_participant().isEmpty()) {
            projectData = project.getParticipants_participant().stream()
                                 .filter(subject -> StringUtils.equals(projectId, subject.getProject()) || subject.getSharing_share().stream().anyMatch(participant -> StringUtils.equals(projectId, participant.getProject())))
                                 .collect(Collectors.toMap(Function.identity(), subject ->
                                         subject.getExperiments_experiment().stream()
                                                .filter(experiment -> StringUtils.equals(projectId, experiment.getProject()) || experiment.getSharing_share().stream().anyMatch(shared -> StringUtils.equals(projectId, shared.getProject())))
                                                .collect(Collectors.toMap(Function.identity(), experiment ->
                                                        experiment instanceof XnatImagesessiondata
                                                        ? ((XnatImagesessiondata) experiment).getAssessors_assessor().stream()
                                                                                             .filter(assessor -> StringUtils.equals(projectId, assessor.getProject()) || assessor.getSharing_share().stream().anyMatch(shared -> StringUtils.equals(projectId, shared.getProject())))
                                                                                             .collect(Collectors.toList())
                                                        : Collections.emptyList(), (e1, e2) -> e1, LinkedHashMap::new)), (e1, e2) -> e1, LinkedHashMap::new));
        } else {
            projectData = Collections.emptyMap();
        }
        log.debug("Found {} subjects for project {}", projectData.size(), projectId);
        context.put("projectData", projectData);
    }
}
