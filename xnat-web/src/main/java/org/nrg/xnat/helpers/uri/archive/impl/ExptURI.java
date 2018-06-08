/*
 * web: org.nrg.xnat.helpers.uri.archive.impl.ExptURI
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
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveURI;
import org.nrg.xnat.helpers.uri.archive.ExperimentURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
public class ExptURI extends ArchiveURI implements ArchiveItemURI, ExperimentURII {
    public ExptURI(Map<String, Object> props, String uri) {
        super(props, uri);
    }

    @Override
    public XnatExperimentdata getExperiment() {
        this.populateExperiment();
        return experiment;
    }

    @Override
    public ArchivableItem getSecurityItem() {
        return getExperiment();
    }

    @Override
    public List<XnatAbstractresourceI> getResources(boolean includeAll) {
        final XnatExperimentdata          experiment = getExperiment();
        final List<XnatAbstractresourceI> resources  = new ArrayList<>(experiment.getResources_resource());

        if (experiment instanceof XnatImagesessiondata && includeAll) {
            final XnatImagesessiondata session = (XnatImagesessiondata) experiment;
            for (final XnatImagescandataI scan : session.getScans_scan()) {
                resources.addAll(scan.getFile());
            }
            for (XnatReconstructedimagedataI scan : session.getReconstructions_reconstructedimage()) {
                resources.addAll(scan.getOut_file());
            }
            for (XnatImageassessordataI scan : session.getAssessors_assessor()) {
                resources.addAll(scan.getOut_file());
            }
        }

        return resources;
    }

    protected void populateExperiment() {
        if (experiment == null) {
            experiment = XnatExperimentdata.getXnatExperimentdatasById(props.get(URIManager.EXPT_ID), null, false);
        }
    }

    private XnatExperimentdata experiment = null;
}
