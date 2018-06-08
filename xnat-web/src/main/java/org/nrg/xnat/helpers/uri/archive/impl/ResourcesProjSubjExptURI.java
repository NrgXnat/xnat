/*
 * web: org.nrg.xnat.helpers.uri.archive.impl.ResourcesProjSubjExptURI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.uri.archive.impl;

import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.ExperimentURII;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.Map;

@SuppressWarnings("Duplicates")
public class ResourcesProjSubjExptURI extends ResourcesProjSubjURI implements ResourceURII, ArchiveItemURI, ExperimentURII {
    public ResourcesProjSubjExptURI(Map<String, Object> props, String uri) {
        super(props, uri);
    }

    @Override
    public ArchivableItem getSecurityItem() {
        return getExperiment();
    }

    @Override
    public XnatAbstractresourceI getXnatResource() {
        final XnatExperimentdata experiment = getExperiment();
        return experiment != null ? getMatchingResource(getExperiment().getResources_resource()) : null;
    }

    public XnatExperimentdata getExperiment() {
        populateExperiment();
        return experiment;
    }

    protected void populateExperiment() {
        populateSubject();

        if (experiment == null) {
            final XnatProjectdata proj = getProject();

            final String exptID = (String) props.get(URIManager.EXPT_ID);

            if (proj != null) {
                experiment = XnatExperimentdata.GetExptByProjectIdentifier(proj.getId(), exptID, null, false);
            }

            if (experiment == null) {
                experiment = XnatExperimentdata.getXnatExperimentdatasById(exptID, null, false);
                if (experiment != null && (proj != null && !experiment.hasProject(proj.getId()))) {
                    experiment = null;
                }
            }
        }
    }

    private XnatExperimentdata experiment = null;
}
