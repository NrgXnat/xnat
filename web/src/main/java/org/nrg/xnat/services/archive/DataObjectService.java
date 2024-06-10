/*
 * web: org.nrg.xnat.services.archive.DataObjectService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.archive;

import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * Manages creation, updating, and deletion of XFT data objects.
 */
public interface DataObjectService {
    /**
     * Creates a new object from the submitted XML.
     *
     * @param xml  The XML representing the new object.
     * @param user The user creating the new object.
     * @return The saved version of the new object.
     * @throws Exception When something goes wrong.
     */
    ItemI createDataObject(final String xml, final UserI user) throws Exception;
}
