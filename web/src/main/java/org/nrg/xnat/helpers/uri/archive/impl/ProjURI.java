/*
 * web: org.nrg.xnat.helpers.uri.archive.impl.ProjURI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.uri.archive.impl;

import com.google.common.collect.Lists;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveURI;
import org.nrg.xnat.helpers.uri.archive.ProjectURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.List;
import java.util.Map;

public class ProjURI extends ArchiveURI implements ArchiveItemURI, ProjectURII {
    public ProjURI(final Map<String, Object> props, final String uri) {
        super(props, uri);
    }

    @Override
    public XnatProjectdata getProject() {
        populateProject();
        return project;
    }

    @Override
    public List<XnatAbstractresourceI> getResources(boolean includeAll) {
        List<XnatAbstractresourceI> res  = Lists.newArrayList();
        final XnatProjectdata       expt = getProject();
        res.addAll(expt.getResources_resource());
        return res;
    }

    @Override
    public ArchivableItem getSecurityItem() {
        return getProject();
    }

    protected void populateProject() {
        if (project == null) {
            project = XnatProjectdata.getProjectByIDorAlias(props.get(URIManager.PROJECT_ID).toString(), null, false);
        }
    }

    private XnatProjectdata project = null;
}
