/*
 * web: org.nrg.xnat.helpers.uri.archive.impl.ProjSubjExptURI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.uri.archive.impl;

import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatReconstructedimagedataI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.ExperimentURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
public class ProjSubjExptURI extends ProjSubjURI implements ArchiveItemURI, ExperimentURII {
    public ProjSubjExptURI(Map<String, Object> props, String uri) {
        super(props, uri);
    }

    @Override
    public XnatExperimentdata getExperiment() {
        populateExperiment();
        return experiment;
    }

    @Override
    public ArchivableItem getSecurityItem() {
        return getExperiment();
    }

    @Override
    public List<XnatAbstractresourceI> getResources(final boolean includeAll) {
        final XnatExperimentdata          experiment = getExperiment();
        final List<XnatAbstractresourceI> resources  = new ArrayList<>(experiment.getResources_resource());

        if (experiment instanceof XnatImagesessiondata && includeAll) {
            final XnatImagesessiondata session = (XnatImagesessiondata) experiment;
            for (final XnatImagescandataI scan : session.getScans_scan()) {
                resources.addAll(scan.getFile());
            }
            for (final XnatReconstructedimagedataI scan : session.getReconstructions_reconstructedimage()) {
                resources.addAll(scan.getOut_file());
            }
            for (final XnatImageassessordataI scan : session.getAssessors_assessor()) {
                resources.addAll(scan.getOut_file());
            }
        }

        return resources;
    }

    protected void populateExperiment() {
        populateSubject();

        if (experiment == null) {
            final XnatProjectdata project = getProject();

            final String experimentId = (String) props.get(URIManager.EXPT_ID);

            if (project != null) {
                experiment = XnatExperimentdata.GetExptByProjectIdentifier(project.getId(), experimentId, null, false);
            }

            if (experiment == null) {
                experiment = XnatExperimentdata.getXnatExperimentdatasById(experimentId, null, false);
                if (experiment != null && (project != null && !experiment.hasProject(project.getId()))) {
                    experiment = null;
                }
            }
        }
    }

    private XnatExperimentdata experiment = null;
}
