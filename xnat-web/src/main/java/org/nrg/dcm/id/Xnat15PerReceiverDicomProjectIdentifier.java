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
import org.nrg.dcm.scp.daos.DicomSCPInstanceService;
import org.nrg.xnat.services.cache.UserProjectCache;

import java.util.ArrayList;
import java.util.List;

/**
 * Xnat15PerReceiverDicomProjectIdentifier extends Xnat15DicomProjectIdentifier to look for dynamic configuration
 * in per-receiver config before site-wide configuration in ConfigService.
 */
@Slf4j
public class Xnat15PerReceiverDicomProjectIdentifier extends Xnat15DicomProjectIdentifier implements AeTitleAndPortAware {
    private final ExtractorFromInstanceProvider _extractorFromInstanceProvider;

    public Xnat15PerReceiverDicomProjectIdentifier(final UserProjectCache userProjectCache, DicomSCPInstanceService dicomSCPInstanceService) {
        super(userProjectCache);
        _extractorFromInstanceProvider = new ExtractorFromInstanceProvider( new RoutingExpressionFromInstanceProvider( dicomSCPInstanceService));
    }

    @Override
    protected List<Extractor> getDynamicExtractors() {
        ExtractorType projectType = ExtractorType.PROJECT;

        List<Extractor> extractors = new ArrayList<>();
        extractors.addAll( _extractorFromInstanceProvider.provide( projectType));
        extractors.addAll( super.getDynamicExtractors());
        return extractors;
    }

    public void setAeTitle( String aeTitle) {
        _extractorFromInstanceProvider.setAeTitle( aeTitle);
    }
    public String getAeTitle() {
        return _extractorFromInstanceProvider.getAeTitle();
    }
    public void setPort( int port) {
        _extractorFromInstanceProvider.setPort( port);
    }
    public int getPort() {
        return _extractorFromInstanceProvider.getPort();
    }
}
