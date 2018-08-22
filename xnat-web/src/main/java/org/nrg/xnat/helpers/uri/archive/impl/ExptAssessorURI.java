/*
 * web: org.nrg.xnat.helpers.uri.archive.impl.ExptAssessorURI
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
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveURI;
import org.nrg.xnat.helpers.uri.archive.AssessedURII;
import org.nrg.xnat.helpers.uri.archive.AssessorURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
public class ExptAssessorURI extends ArchiveURI implements ArchiveItemURI, AssessedURII, AssessorURII {
    public ExptAssessorURI(Map<String, Object> props, String uri) {
        super(props, uri);
    }

    public XnatImagesessiondata getSession() {
        populate();
        return session;
    }

    public XnatImageassessordata getAssessor() {
        populate();
        return experiment;
    }

    public ArchivableItem getSecurityItem() {
        return getAssessor();
    }

    @Override
    public List<XnatAbstractresourceI> getResources(boolean includeAll) {
        final XnatImageassessordata assessor = getAssessor();
        return Lists.newArrayList(Iterables.concat(assessor.getResources_resource(), assessor.getOut_file()));
    }

    protected void populate() {
        if (experiment == null) {
            final String sessID = (String) props.get(URIManager.ASSESSED_ID);

            if (session == null) {
                session = (XnatImagesessiondata) XnatExperimentdata.getXnatExperimentdatasById(sessID, null, false);
            }

            final String exptID = (String) props.get(URIManager.EXPT_ID);

            if (experiment == null) {
                experiment = (XnatImageassessordata) XnatExperimentdata.getXnatExperimentdatasById(exptID, null, false);
            }
        }
    }

    private XnatImageassessordata experiment = null;
    private XnatImagesessiondata  session    = null;
}
