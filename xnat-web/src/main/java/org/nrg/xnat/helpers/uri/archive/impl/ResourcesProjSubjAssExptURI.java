/*
 * web: org.nrg.xnat.helpers.uri.archive.impl.ResourcesProjSubjAssExptURI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.uri.archive.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.AssessedURII;
import org.nrg.xnat.helpers.uri.archive.AssessorURII;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.helpers.uri.archive.ResourcesProjSubjSessionURIA;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
@Slf4j
public class ResourcesProjSubjAssExptURI extends ResourcesProjSubjSessionURIA implements AssessedURII, ResourceURII, ArchiveItemURI, AssessorURII {
    public ResourcesProjSubjAssExptURI(Map<String, Object> props, String uri) {
        super(props, uri);
    }

    @Override
    public XnatImageassessordata getAssessor() {
        populateAssessor();
        return assessor;
    }

    @Override
    public ArchivableItem getSecurityItem() {
        return getAssessor();
    }

    @Override
    public XnatAbstractresourceI getXnatResource() {
        final XnatImageassessordata assessor = getAssessor();
        if (assessor == null) {
            return null;
        }

        final List<XnatAbstractresourceI> resources = getAssessorResources(assessor, StringUtils.defaultIfBlank((String) props.get(URIManager.TYPE), "out"));
        return getMatchingResource(resources);
    }

    protected void populateAssessor() {
        populateSession();

        if (assessor == null) {
            final XnatProjectdata proj = getProject();

            final String exptID = (String) props.get(URIManager.EXPT_ID);

            if (proj != null) {
                assessor = (XnatImageassessordata) XnatExperimentdata.GetExptByProjectIdentifier(proj.getId(), exptID, null, false);
            }

            if (assessor == null) {
                assessor = (XnatImageassessordata) XnatExperimentdata.getXnatExperimentdatasById(exptID, null, false);
                if (assessor != null && (proj != null && !assessor.hasProject(proj.getId()))) {
                    assessor = null;
                }
            }
        }
    }

    private XnatImageassessordata assessor = null;
}
