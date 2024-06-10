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

/**
 * ClassicDicomObjectIdentifier
 *
 * Now brought to you by the Xnat15PerReceiverDicomProjectIdentifier DicomProjectIdentifier
 * and the XnatDefaultPerReceiverDicomObjectIdentifier which extends XnatDefaultDicomObjectIdentifier to look for
 * dynamic configuration by receiver before falling back to configService config and then fixed extractors.
 *
 */
public class ClassicDicomObjectIdentifier extends XnatDefaultPerReceiverDicomObjectIdentifier {
    public ClassicDicomObjectIdentifier(final String name,
                                        final XnatUserProvider userProvider,
                                        final UserProjectCache userProjectCache) {
        super(name,
                userProvider,
                new Xnat15PerReceiverDicomProjectIdentifier(userProjectCache));
    }
}
