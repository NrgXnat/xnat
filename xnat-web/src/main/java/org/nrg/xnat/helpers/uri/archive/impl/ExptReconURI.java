/*
 * web: org.nrg.xnat.helpers.uri.archive.impl.ExptReconURI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.uri.archive.impl;

import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatReconstructedimagedata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveURI;
import org.nrg.xnat.helpers.uri.archive.AssessedURII;
import org.nrg.xnat.helpers.uri.archive.ReconURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
public class ExptReconURI extends ArchiveURI implements ArchiveItemURI, AssessedURII, ReconURII {
    public ExptReconURI(Map<String, Object> props, String uri) {
        super(props, uri);
    }

    @Override
    public XnatImagesessiondata getSession() {
        populate();
        return session;
    }

    @Override
    public XnatReconstructedimagedata getRecon() {
        populate();
        return reconstruction;
    }

    @Override
    public ArchivableItem getSecurityItem() {
        return getSession();
    }

    @Override
    public List<XnatAbstractresourceI> getResources(boolean includeAll) {
        return new ArrayList<>(getRecon().getOut_file());
    }

    protected void populate() {
        if (reconstruction == null) {
            final String exptID = (String) props.get(URIManager.ASSESSED_ID);

            if (session == null) {
                session = (XnatImagesessiondata) XnatExperimentdata.getXnatExperimentdatasById(exptID, null, false);
            }

            final String reconID = (String) props.get(URIManager.RECON_ID);

            if (reconstruction == null && reconID != null) {
                reconstruction = XnatReconstructedimagedata.getXnatReconstructedimagedatasById(reconID, null, false);
            }
        }
    }

    private XnatReconstructedimagedata reconstruction = null;
    private XnatImagesessiondata       session        = null;
}
