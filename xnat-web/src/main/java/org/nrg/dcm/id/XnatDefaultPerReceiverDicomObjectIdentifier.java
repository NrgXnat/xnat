package org.nrg.dcm.id;

import org.nrg.dcm.Extractor;
import org.nrg.dcm.scp.daos.DicomSCPInstanceService;
import org.nrg.xdat.security.user.XnatUserProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * XnatDefaultPerReceiverDicomObjectIdentifier extends XnatDefaultPerReceiverDicomObjectIdentifier to look for dynamic configuration
 * in per-receiver config before site-wide configuration in ConfigService.
 */
public class XnatDefaultPerReceiverDicomObjectIdentifier extends XnatDefaultDicomObjectIdentifier implements AeTitleAndPortAware {
    private final ExtractorFromInstanceProvider _extractorFromInstanceProvider;

    public XnatDefaultPerReceiverDicomObjectIdentifier(String name,
                                                       XnatUserProvider userProvider,
                                                       DicomProjectIdentifier identifier,
                                                       DicomSCPInstanceService dicomSCPInstanceService) {
        super(name, userProvider, identifier);
        _extractorFromInstanceProvider = new ExtractorFromInstanceProvider( new RoutingExpressionFromInstanceProvider( dicomSCPInstanceService));
    }

    @Override
    protected List<Extractor> getDynamicExtractors(ExtractorType type) {
        List<Extractor> extractors = new ArrayList<>();
        extractors.addAll( _extractorFromInstanceProvider.provide(type));
        extractors.addAll( super.getDynamicExtractors( type));
        return extractors;
    }

    @Override
    public void setAeTitle(String aeTitle) {
        _extractorFromInstanceProvider.setAeTitle( aeTitle);
        if( AeTitleAndPortAware.class.isInstance( getProjectIdentifier())) {
            AeTitleAndPortAware aware = (AeTitleAndPortAware) getProjectIdentifier();
            aware.setAeTitle( aeTitle);
        }
    }

    @Override
    public String getAeTitle() {
        return _extractorFromInstanceProvider.getAeTitle();
    }

    @Override
    public void setPort(int port) {
        _extractorFromInstanceProvider.setPort( port);
        if( AeTitleAndPortAware.class.isInstance( getProjectIdentifier())) {
            AeTitleAndPortAware aware = (AeTitleAndPortAware) getProjectIdentifier();
            aware.setPort( port);
        }
    }

    @Override
    public boolean isCustomRoutingSupported() {
        return true;
    }

    @Override
    public int getPort() {
        return _extractorFromInstanceProvider.getPort();
    }

}
