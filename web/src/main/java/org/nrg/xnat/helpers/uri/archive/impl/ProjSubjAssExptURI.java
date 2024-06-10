/*
 * web: org.nrg.xnat.helpers.uri.archive.impl.ProjSubjAssExptURI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.uri.archive.impl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.AssessedURII;
import org.nrg.xnat.helpers.uri.archive.AssessorURII;
import org.nrg.xnat.helpers.uri.archive.ProjSubjSessionURIA;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
public class ProjSubjAssExptURI extends ProjSubjSessionURIA implements ArchiveItemURI, AssessedURII, AssessorURII {
    public ProjSubjAssExptURI(final Map<String, Object> props, final String uri) {
        super(props, uri);
    }

    @Override
    public XnatImageassessordata getAssessor() {
        populateExperiment();
        return expt;
    }

    @Override
    public ArchivableItem getSecurityItem() {
        return getAssessor();
    }

    @Override
    public List<XnatAbstractresourceI> getResources(final boolean includeAll) {
        final XnatImageassessordata assessor = getAssessor();
        return Lists.newArrayList(Iterables.concat(assessor.getResources_resource(), assessor.getOut_file()));
    }

    protected void populateExperiment() {
        populateSession();

        if (expt == null) {
            final XnatProjectdata proj = getProject();

            final String exptID = (String) props.get(URIManager.EXPT_ID);

            if (proj != null) {
                expt = (XnatImageassessordata) XnatExperimentdata.GetExptByProjectIdentifier(proj.getId(), exptID, null, false);
            }

            if (expt == null) {
                expt = (XnatImageassessordata) XnatExperimentdata.getXnatExperimentdatasById(exptID, null, false);
                if (expt != null && (proj != null && !expt.hasProject(proj.getId()))) {
                    expt = null;
                }
            }
        }
    }

    private XnatImageassessordata expt = null;
}
