/*
 * web: org.nrg.xnat.helpers.uri.archive.impl.ResourcesExptScanURI
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
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.AssessedURII;
import org.nrg.xnat.helpers.uri.archive.ResourceURIA;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.helpers.uri.archive.ScanURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.Map;

public class ResourcesExptScanURI extends ResourceURIA implements ArchiveItemURI, AssessedURII, ResourceURII, ScanURII {
    public ResourcesExptScanURI(Map<String, Object> props, String uri) {
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
    public XnatAbstractresourceI getXnatResource() {
        final XnatImagescandata scan = getScan();
        return scan != null ? getMatchingResource(scan.getFile()) : null;
    }

    @Override
    public XnatProjectdata getProject() {
        return getSession().getProjectData();
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
