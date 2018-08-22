/*
 * web: org.nrg.xnat.helpers.uri.archive.ResourceURII
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.uri.archive;

import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;

public interface ResourceURII extends ArchiveItemURI {
    /**
     * Gets the resource corresponding to the URI.
     *
     * @return The {@link XnatAbstractresourceI resource object} for the URI. Returns null if the specified resource doesn't exist.
     */
    XnatAbstractresourceI getXnatResource();

    /**
     * Gets the project with which the resource is associated.
     *
     * @return The associated project.
     */
    XnatProjectdata getProject();

    /**
     * Gets the {@link XnatAbstractresourceI#getXnatAbstractresourceId() ID of the abstract resource object}.
     *
     * @return The abstract resource object's ID.
     */
    int getXnatAbstractresourceId();

    /**
     * Label of this resource
     *
     * @return The resource label.
     */
    String getResourceLabel();

    /**
     * Path to data file within this resource
     *
     * @return The path to the resource data.
     */
    String getResourceFilePath();

}
