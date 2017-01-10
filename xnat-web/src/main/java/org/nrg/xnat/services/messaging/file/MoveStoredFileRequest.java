/*
 * web: org.nrg.xnat.services.messaging.file.MoveStoredFileRequest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.messaging.file;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.file.StoredFile;
import org.nrg.xnat.helpers.resource.XnatResourceInfo;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;

public class MoveStoredFileRequest implements Serializable {
    public MoveStoredFileRequest(ResourceModifierA resourceModifier, Object resourceIdentifier, List<FileWriterWrapperI> writers, UserI user, Number workflowId, boolean delete, String[] notifyList, String type, String filepath, XnatResourceInfo resourceInfo, boolean extract) {
        this.resourceModifier = resourceModifier;
        this.resourceIdentifier = resourceIdentifier != null ? resourceIdentifier.toString() : null;
        for (FileWriterWrapperI writer : writers) {
            try {
                StoredFile storedFile = (StoredFile) writer;
                this.writers.add(storedFile);
            } catch (Exception e) { /* Not a stored file for some reason */ }
        }
        this.user = user;
        this.workflowId = workflowId.toString();
        this.delete = delete;
        this.notifyList = notifyList;
        this.type = type;
        this.filepath = filepath;
        this.resourceInfo = resourceInfo;
        this.extract = extract;
    }

    public ResourceModifierA getResourceModifier() {
        return resourceModifier;
    }

    public String getResourceIdentifier() {
        return resourceIdentifier;
    }

    public List<StoredFile> getWriters() {
        return writers;
    }

    public UserI getUser() {
        return user;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public boolean isDelete() {
        return delete;
    }

    public String[] getNotifyList() {
        return notifyList;
    }

    public String getType() {
        return type;
    }

    public String getFilepath() {
        return filepath;
    }

    public XnatResourceInfo getResourceInfo() {
        return resourceInfo;
    }

    public boolean isExtract() {
        return extract;
    }

    private static final long serialVersionUID = 42L;
    private final ResourceModifierA resourceModifier;
    private final String resourceIdentifier;
    private final List<StoredFile> writers = new ArrayList<StoredFile>();
    private final UserI user;
    private final String workflowId;
    private final boolean delete;
    private final String[] notifyList;
    private final String type;
    private final String filepath;
    private final XnatResourceInfo resourceInfo;
    private final boolean extract;
}
