/*
 * web: org.nrg.xnat.helpers.uri.archive.impl.ExptScanURI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.uri.archive.impl;

import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveURI;
import org.nrg.xnat.helpers.uri.archive.AssessedURII;
import org.nrg.xnat.helpers.uri.archive.ScanURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExptScanURI extends ArchiveURI implements ArchiveItemURI, AssessedURII, ScanURII {
    public ExptScanURI(Map<String, Object> props, String uri) {
        super(props, uri);
    }

    @Override
    public XnatImagesessiondata getSession() {
        populate();
        return session;
    }

    @Override
    public XnatImagescandata getScan() {
        populate();
        return scan;
    }

    @Override
    public ArchivableItem getSecurityItem() {
        return getSession();
    }

    @Override
    public List<XnatAbstractresourceI> getResources(final boolean includeAll) {
        return new ArrayList<>(getScan().getFile());
    }

    protected void populate() {
        if (session == null) {
            session = (XnatImagesessiondata) XnatExperimentdata.getXnatExperimentdatasById(props.get(URIManager.ASSESSED_ID), null, false);
        }

        if (scan == null) {
            scan = getScan(session, (String) props.get(URIManager.SCAN_ID));
        }
    }

    private XnatImagescandata    scan    = null;
    private XnatImagesessiondata session = null;
}
