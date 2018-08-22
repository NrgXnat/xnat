/*
 * web: org.nrg.xnat.helpers.uri.archive.impl.ResourcesSubjURI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.uri.archive.impl;

import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.ResourceURIA;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.helpers.uri.archive.SubjectURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.Map;

public class ResourcesSubjURI extends ResourceURIA implements ArchiveItemURI, ResourceURII, SubjectURII {
    public ResourcesSubjURI(Map<String, Object> props, String uri) {
        super(props, uri);
    }

    @Override
    public ArchivableItem getSecurityItem() {
        return getSubject();
    }

    @Override
    public XnatAbstractresourceI getXnatResource() {
        final XnatSubjectdata subject = getSubject();
        return subject != null ? getMatchingResource(subject.getResources_resource()) : null;
    }

    @Override
    public XnatProjectdata getProject() {
        return this.getSubject().getProjectData();
    }

    public XnatSubjectdata getSubject() {
        this.populate();
        return subj;
    }

    protected void populate() {
        if (subj == null) {

            final String subjID = (String) props.get(URIManager.SUBJECT_ID);

            if (subj == null) {
                subj = XnatSubjectdata.getXnatSubjectdatasById(subjID, null, false);
            }
        }
    }

    private XnatSubjectdata subj = null;
}
