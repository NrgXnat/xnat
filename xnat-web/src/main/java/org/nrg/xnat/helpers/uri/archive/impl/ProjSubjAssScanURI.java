/*
 * web: org.nrg.xnat.helpers.uri.archive.impl.ProjSubjAssScanURI
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
import org.nrg.xnat.helpers.uri.archive.ProjSubjSessionURIA;
import org.nrg.xnat.helpers.uri.archive.ScanURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProjSubjAssScanURI extends ProjSubjSessionURIA implements ArchiveItemURI, AssessedURII, ScanURII {
    public ProjSubjAssScanURI(Map<String, Object> props, String uri) {
        super(props, uri);
    }

    @Override
    public XnatImagescandata getScan() {
        populateScan();
        return scan;
    }

    @Override
    public ArchivableItem getSecurityItem() {
        return getSession();
    }

    @Override
    public List<XnatAbstractresourceI> getResources(boolean includeAll) {
        return new ArrayList<>(getScan().getFile());
    }

    protected void populateScan() {
        populateSession();

        if (scan == null) {
            scan = getScan(getSession(), (String) props.get(URIManager.SCAN_ID));
        }
    }

    private XnatImagescandata scan = null;
}
