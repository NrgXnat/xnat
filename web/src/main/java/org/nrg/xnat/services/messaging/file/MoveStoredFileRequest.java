/*
 * web: org.nrg.xnat.services.messaging.file.MoveStoredFileRequest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.messaging.file;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.file.StoredFile;
import org.nrg.xnat.helpers.resource.XnatResourceInfo;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(prefix = "_")
public class MoveStoredFileRequest implements Serializable {
    public MoveStoredFileRequest(final ResourceModifierA resourceModifier, final Object resourceIdentifier, final List<FileWriterWrapperI> writers, final UserI user, final Number workflowId, final boolean delete, final String[] notifyList, final String type, final String filePath, final XnatResourceInfo resourceInfo, final boolean extract) {
        this(resourceModifier, resourceIdentifier, writers, user, workflowId, delete, notifyList, type, filePath, resourceInfo, extract, null);
    }

    public MoveStoredFileRequest(final ResourceModifierA resourceModifier, final Object resourceIdentifier, final List<FileWriterWrapperI> writers, final UserI user, final Number workflowId, final boolean delete, final String[] notifyList, final String type, final String filePath, final XnatResourceInfo resourceInfo, final boolean extract, final String project) {
        _resourceModifier = resourceModifier;
        _resourceIdentifier = resourceIdentifier != null ? resourceIdentifier.toString() : null;
        _user = user;
        _workflowId = workflowId.toString();
        _delete = delete;
        _notifyList = notifyList;
        _type = type;
        _filePath = filePath;
        _resourceInfo = resourceInfo;
        _extract = extract;
        _project = project;
        _writers.addAll(Lists.newArrayList((Iterables.filter(Lists.transform(writers, new Function<FileWriterWrapperI, StoredFile>() {
            @Override
            public StoredFile apply(@Nullable final FileWriterWrapperI writer) {
                try {
                    return (StoredFile) writer;
                } catch (Exception e) {
                    // Not a stored file for some reason
                    return null;
                }
            }
        }), Predicates.<StoredFile>notNull()))));
    }

    @Override
    public String toString() {
        return "Move stored file request on file " + _filePath + " in project " + _project + " requested by " + _user.getUsername();
    }

    private static final long serialVersionUID = 42L;

    private final ResourceModifierA _resourceModifier;
    private final String            _resourceIdentifier;
    private final UserI             _user;
    private final String            _workflowId;
    private final boolean           _delete;
    private final String[]          _notifyList;
    private final String            _type;
    private final String            _filePath;
    private final XnatResourceInfo  _resourceInfo;
    private final boolean           _extract;
    private final String            _project;
    private final List<StoredFile>  _writers = new ArrayList<>();
}
