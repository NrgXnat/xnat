/*
 * web: org.nrg.dcm.id.ClassicDicomObjectIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.id;

import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.services.cache.UserProjectCache;

public class ClassicDicomObjectIdentifier extends XnatDefaultDicomObjectIdentifier {
    public ClassicDicomObjectIdentifier(final String name, final XnatUserProvider userProvider, final UserProjectCache userProjectCache) {
        super(name, userProvider, new Xnat15DicomProjectIdentifier(userProjectCache));
    }
}
