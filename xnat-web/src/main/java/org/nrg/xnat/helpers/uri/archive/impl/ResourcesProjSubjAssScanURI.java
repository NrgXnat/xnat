/*
 * web: org.nrg.xnat.helpers.uri.archive.impl.ResourcesProjSubjAssScanURI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.uri.archive.impl;

import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.AssessedURII;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.helpers.uri.archive.ResourcesProjSubjSessionURIA;
import org.nrg.xnat.helpers.uri.archive.ScanURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.Map;

public class ResourcesProjSubjAssScanURI extends ResourcesProjSubjSessionURIA implements AssessedURII, ResourceURII, ArchiveItemURI, ScanURII {
    public ResourcesProjSubjAssScanURI(Map<String, Object> props, String uri) {
        super(props, uri);
    }

    @Override
    public ArchivableItem getSecurityItem() {
        return getSession();
    }

    @Override
    public XnatAbstractresourceI getXnatResource() {
        final XnatImagescandata scan = getScan();
        return scan != null ? getMatchingResource(scan.getFile()) : null;
    }

    public XnatImagescandata getScan() {
        populateScan();
        return scan;
    }

    protected void populateScan() {
        populateSession();

        if (scan == null) {
            scan = getScan(getSession(), (String) props.get(URIManager.SCAN_ID));
        }
    }

    private XnatImagescandata scan = null;
}
