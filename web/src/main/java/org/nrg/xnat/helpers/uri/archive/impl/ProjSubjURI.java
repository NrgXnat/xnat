/*
 * web: org.nrg.xnat.helpers.uri.archive.impl.ProjSubjURI
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
import org.nrg.xnat.helpers.uri.archive.SubjectURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
public class ProjSubjURI extends ProjURI implements ArchiveItemURI, SubjectURII {
    public ProjSubjURI(Map<String, Object> props, String uri) {
        super(props, uri);
    }

    @Override
    public XnatSubjectdata getSubject() {
        populateSubject();
        return subject;
    }

    @Override
    public ArchivableItem getSecurityItem() {
        return getSubject();
    }

    @Override
    public List<XnatAbstractresourceI> getResources(final boolean includeAll) {
        return new ArrayList<>(getSubject().getResources_resource());
    }

    protected void populateSubject() {
        populateProject();

        if (subject == null) {
            final XnatProjectdata project = getProject();

            final String subjectId = (String) props.get(URIManager.SUBJECT_ID);

            if (project != null) {
                subject = XnatSubjectdata.GetSubjectByProjectIdentifier(project.getId(), subjectId, null, false);
            }

            if (subject == null) {
                subject = XnatSubjectdata.getXnatSubjectdatasById(subjectId, null, false);
                if (subject != null && (project != null && !subject.hasProject(project.getId()))) {
                    subject = null;
                }
            }
        }
    }

    private XnatSubjectdata subject = null;
}
