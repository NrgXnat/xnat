/*
 * web: org.nrg.xnat.helpers.uri.archive.impl.ResourcesExptURI
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
import org.nrg.xnat.helpers.uri.archive.ResourceURIA;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.Map;

public class ResourcesExptURI extends ResourceURIA implements ArchiveItemURI, ResourceURII, ExperimentURII {
    public ResourcesExptURI(Map<String, Object> props, String uri) {
        super(props, uri);
    }

    @Override
    public XnatExperimentdata getExperiment() {
        populate();
        return experiment;
    }

    @Override
    public ArchivableItem getSecurityItem() {
        return getExperiment();
    }

    @Override
    public XnatAbstractresourceI getXnatResource() {
        final XnatExperimentdata experiment = getExperiment();
        return experiment != null ? getMatchingResource(experiment.getResources_resource()) : null;
    }

    @Override
    public XnatProjectdata getProject() {
        return this.getExperiment().getProjectData();
    }

    protected void populate() {
        if (experiment == null) {

            final String exptID = (String) props.get(URIManager.EXPT_ID);

            if (experiment == null) {
                experiment = XnatExperimentdata.getXnatExperimentdatasById(exptID, null, false);
            }
        }
    }

    private XnatExperimentdata experiment = null;
}
