/*
 * web: org.nrg.xnat.helpers.uri.archive.impl.ProjSubjAssReconURI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.uri.archive.impl;

import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatReconstructedimagedata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.AssessedURII;
import org.nrg.xnat.helpers.uri.archive.ProjSubjSessionURIA;
import org.nrg.xnat.helpers.uri.archive.ReconURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProjSubjAssReconURI extends ProjSubjSessionURIA implements ArchiveItemURI, AssessedURII, ReconURII {
    public ProjSubjAssReconURI(final Map<String, Object> props, final String uri) {
        super(props, uri);
    }

    @Override
    public XnatReconstructedimagedata getRecon() {
        populateRecon();
        return reconstruction;
    }

    @Override
    public ArchivableItem getSecurityItem() {
        return getSession();
    }

    @Override
    public List<XnatAbstractresourceI> getResources(final boolean includeAll) {
        return new ArrayList<>(getRecon().getOut_file());
    }

    protected void populateRecon() {
        populateSession();

        if (reconstruction == null) {
            final String reconID = (String) props.get(URIManager.RECON_ID);

            if (reconstruction == null && reconID != null) {
                reconstruction = XnatReconstructedimagedata.getXnatReconstructedimagedatasById(reconID, null, false);
            }
        }
    }

    private XnatReconstructedimagedata reconstruction = null;
}
