/*
 * web: org.nrg.xnat.helpers.uri.archive.impl.ResourcesProjSubjURI
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
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.helpers.uri.archive.SubjectURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.Map;

@SuppressWarnings("Duplicates")
public class ResourcesProjSubjURI extends ResourcesProjURI implements ResourceURII, ArchiveItemURI, SubjectURII {
    public ResourcesProjSubjURI(Map<String, Object> props, String uri) {
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

    public XnatSubjectdata getSubject() {
        populateSubject();
        return subject;
    }

    protected void populateSubject() {
        populateProject();

        if (subject == null) {
            final XnatProjectdata proj = getProject();

            final String subID = (String) props.get(URIManager.SUBJECT_ID);

            if (proj != null) {
                subject = XnatSubjectdata.GetSubjectByProjectIdentifier(proj.getId(), subID, null, false);
            }

            if (subject == null) {
                subject = XnatSubjectdata.getXnatSubjectdatasById(subID, null, false);
                if (subject != null && (proj != null && !subject.hasProject(proj.getId()))) {
                    subject = null;
                }
            }
        }
    }

    private XnatSubjectdata subject = null;
}
