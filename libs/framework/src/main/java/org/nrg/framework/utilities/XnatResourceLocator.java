/*
 * framework: org.nrg.framework.utilities.XnatResourceLocator
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

public interface XnatResourceLocator {
    Resource getResource() throws IOException;
    List<Resource> getResources() throws IOException;
}
