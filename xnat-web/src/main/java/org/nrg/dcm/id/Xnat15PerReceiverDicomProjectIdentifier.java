/*
 * web: org.nrg.dcm.id.Xnat15DicomProjectIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.id;

import lombok.extern.slf4j.Slf4j;
import org.nrg.dcm.Extractor;
import org.nrg.dcm.id.CompositeDicomObjectIdentifier.ExtractorType;
import org.nrg.xnat.services.cache.UserProjectCache;

import java.util.ArrayList;
import java.util.List;

/**
 * Xnat15PerReceiverDicomProjectIdentifier extends Xnat15DicomProjectIdentifier to look for dynamic configuration
 * in per-receiver config before site-wide configuration in ConfigService.
 */
@Slf4j
public class Xnat15PerReceiverDicomProjectIdentifier extends Xnat15DicomProjectIdentifier
        implements ReceiverAwareProjectIdentifier<Xnat15PerReceiverDicomProjectIdentifier> {
    private final UserProjectCache _userProjectCache;
    private ExtractorFromInstanceProvider _extractorFromInstanceProvider = null;

    public Xnat15PerReceiverDicomProjectIdentifier(final UserProjectCache userProjectCache) {
        super(userProjectCache);
        _userProjectCache = userProjectCache;
    }

    public Xnat15PerReceiverDicomProjectIdentifier(UserProjectCache userProjectCache,
                                                   ExtractorFromInstanceProvider extractor) {
        this(userProjectCache);
        _extractorFromInstanceProvider = extractor;
    }

    @Override
    protected List<Extractor> getDynamicExtractors() {
        ExtractorType projectType = ExtractorType.PROJECT;

        List<Extractor> extractors = new ArrayList<>();
        if (_extractorFromInstanceProvider != null) {
            extractors.addAll(_extractorFromInstanceProvider.provide(projectType));
        }
        extractors.addAll(super.getDynamicExtractors());
        return extractors;
    }

    @Override
    public Xnat15PerReceiverDicomProjectIdentifier withExtractor(ExtractorFromInstanceProvider extractor) {
        return new Xnat15PerReceiverDicomProjectIdentifier(_userProjectCache, extractor);
    }
}
