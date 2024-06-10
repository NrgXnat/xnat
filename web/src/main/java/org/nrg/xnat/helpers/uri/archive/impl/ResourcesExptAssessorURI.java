/*
 * web: org.nrg.xnat.helpers.uri.archive.impl.ResourcesExptAssessorURI
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
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.AssessedURII;
import org.nrg.xnat.helpers.uri.archive.AssessorURII;
import org.nrg.xnat.helpers.uri.archive.ResourceURIA;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
@Slf4j
public class ResourcesExptAssessorURI extends ResourceURIA implements ArchiveItemURI, AssessedURII, ResourceURII, AssessorURII {
    public ResourcesExptAssessorURI(Map<String, Object> props, String uri) {
        super(props, uri);
    }

    @Override
    public XnatImagesessiondata getSession() {
        populate();
        return session;
    }

    @Override
    public XnatImageassessordata getAssessor() {
        populate();
        return experiment;
    }

    @Override
    public XnatProjectdata getProject() {
        return getAssessor().getProjectData();
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

    @Override
    public ArchivableItem getSecurityItem() {
        return getAssessor();
    }

    protected void populate() {
        if (experiment == null) {
            final String sessID = (String) props.get(URIManager.ASSESSED_ID);

            if (session == null) {
                session = (XnatImagesessiondata) XnatExperimentdata.getXnatExperimentdatasById(sessID, null, false);
            }

            final String exptID = (String) props.get(URIManager.EXPT_ID);

            if (experiment == null) {
                experiment = getImageAssessorByIdOrLabel(session, exptID);
            }
        }
    }

    private XnatImageassessordata experiment = null;
    private XnatImagesessiondata  session    = null;
}
