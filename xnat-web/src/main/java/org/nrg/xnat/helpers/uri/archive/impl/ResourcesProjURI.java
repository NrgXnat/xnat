/*
 * web: org.nrg.xnat.helpers.uri.archive.impl.ResourcesProjURI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.uri.archive.impl;

import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.ProjectURII;
import org.nrg.xnat.helpers.uri.archive.ResourceURIA;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.Map;

public class ResourcesProjURI extends ResourceURIA implements ArchiveItemURI, ResourceURII, ProjectURII {
    public ResourcesProjURI(final Map<String, Object> props, final String uri) {
        super(props, uri);
    }

    @Override
    public ArchivableItem getSecurityItem() {
        return getProject();
    }

    @Override
    public XnatAbstractresourceI getXnatResource() {
        final XnatProjectdata project = getProject();
        return project != null ? getMatchingResource(getProject().getResources_resource()) : null;
    }

    public XnatProjectdata getProject() {
        populateProject();
        return project;
    }

    protected void populateProject() {
        if (project == null) {
            project = XnatProjectdata.getProjectByIDorAlias(props.get(URIManager.PROJECT_ID).toString(), null, false);
        }
    }

    private XnatProjectdata project = null;
}
