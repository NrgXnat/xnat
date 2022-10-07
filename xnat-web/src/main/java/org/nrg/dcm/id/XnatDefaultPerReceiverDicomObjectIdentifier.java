package org.nrg.dcm.id;

import org.nrg.dcm.Extractor;
import org.nrg.dcm.scp.DicomSCPInstance;
import org.nrg.xdat.security.user.XnatUserProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * XnatDefaultPerReceiverDicomObjectIdentifier extends XnatDefaultPerReceiverDicomObjectIdentifier to look for dynamic configuration
 * in per-receiver config before site-wide configuration in ConfigService.
 */
public class XnatDefaultPerReceiverDicomObjectIdentifier extends XnatDefaultDicomObjectIdentifier
        implements ReceiverAwareIdentifier<XnatDefaultPerReceiverDicomObjectIdentifier> {
    private final XnatUserProvider _userProvider;
    private ExtractorFromInstanceProvider _extractorFromInstanceProvider = null;

    public XnatDefaultPerReceiverDicomObjectIdentifier(String name,
                                                       XnatUserProvider userProvider,
                                                       DicomProjectIdentifier identifier) {
        super(name, userProvider, identifier);
        _userProvider = userProvider;
    }

    public XnatDefaultPerReceiverDicomObjectIdentifier(String name,
                                                       XnatUserProvider userProvider,
                                                       DicomProjectIdentifier identifier,
                                                       ExtractorFromInstanceProvider extractorFromInstanceProvider) {
        this(name, userProvider, identifier);
        _extractorFromInstanceProvider = extractorFromInstanceProvider;
    }

    @Override
    public XnatDefaultPerReceiverDicomObjectIdentifier forInstance(DicomSCPInstance dicomSCPInstance) {
        ExtractorFromInstanceProvider extractorFromInstanceProvider = new ExtractorFromInstanceProvider(
                new RoutingExpressionFromInstanceProvider(dicomSCPInstance));
        DicomProjectIdentifier identifier = getProjectIdentifier();
        if (identifier instanceof ReceiverAwareProjectIdentifier) {
            identifier = ((ReceiverAwareProjectIdentifier<? extends DicomProjectIdentifier>) identifier)
                    .withExtractor(extractorFromInstanceProvider);
        }
        return new XnatDefaultPerReceiverDicomObjectIdentifier(getName(), _userProvider, identifier,
                extractorFromInstanceProvider);
    }

    @Override
    protected List<Extractor> getDynamicExtractors(ExtractorType type) {
        List<Extractor> extractors = new ArrayList<>();
        if (_extractorFromInstanceProvider != null) {
            extractors.addAll(_extractorFromInstanceProvider.provide(type));
        }
        extractors.addAll(super.getDynamicExtractors(type));
        return extractors;
    }

    @Override
    public boolean isCustomRoutingSupported() {
        return true;
    }
}
