/*
 * web: org.nrg.xnat.helpers.uri.archive.impl.ResourcesProjSubjAssReconURI
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
import org.nrg.xdat.om.XnatReconstructedimagedata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.AssessedURII;
import org.nrg.xnat.helpers.uri.archive.ReconURII;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.helpers.uri.archive.ResourcesProjSubjSessionURIA;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.List;
import java.util.Map;

@Slf4j
public class ResourcesProjSubjAssReconURI extends ResourcesProjSubjSessionURIA implements AssessedURII, ResourceURII, ArchiveItemURI, ReconURII {
    public ResourcesProjSubjAssReconURI(Map<String, Object> props, String uri) {
        super(props, uri);
    }

    @Override
    public XnatReconstructedimagedata getRecon() {
        this.populateRecon();
        return this.reconstruction;
    }

    @Override
    public ArchivableItem getSecurityItem() {
        return getSession();
    }

    @Override
    public XnatAbstractresourceI getXnatResource() {
        final XnatReconstructedimagedata reconstruction = getRecon();
        if (reconstruction == null) {
            return null;
        }

        final List<XnatAbstractresourceI> resources = getReconstructionResources(reconstruction, StringUtils.defaultIfBlank((String) props.get(URIManager.TYPE), "out"));
        return getMatchingResource(resources);
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
