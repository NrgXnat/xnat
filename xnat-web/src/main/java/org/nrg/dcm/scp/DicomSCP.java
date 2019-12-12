package org.nrg.dcm.scp;

import static org.dcm4che2.data.UID.ExplicitVRBigEndian;
import static org.dcm4che2.data.UID.ExplicitVRLittleEndian;
import static org.dcm4che2.data.UID.ImplicitVRLittleEndian;
import static org.dcm4che2.data.UID.JPEG2000;
import static org.dcm4che2.data.UID.JPEG2000LosslessOnly;
import static org.dcm4che2.data.UID.JPEG2000Part2MultiComponent;
import static org.dcm4che2.data.UID.JPEG2000Part2MultiComponentLosslessOnly;
import static org.dcm4che2.data.UID.JPEGBaseline1;
import static org.dcm4che2.data.UID.JPEGExtended24;
import static org.dcm4che2.data.UID.JPEGLSLossless;
import static org.dcm4che2.data.UID.JPEGLSLossyNearLossless;
import static org.dcm4che2.data.UID.JPEGLossless;
import static org.dcm4che2.data.UID.JPEGLosslessNonHierarchical14;
import static org.dcm4che2.data.UID.JPIPReferenced;
import static org.dcm4che2.data.UID.JPIPReferencedDeflate;
import static org.dcm4che2.data.UID.MPEG2;
import static org.dcm4che2.data.UID.RFC2557MIMEEncapsulation;
import static org.dcm4che2.data.UID.RLELossless;
import static org.dcm4che2.data.UID.VerificationSOPClass;
import static org.dcm4che2.data.UID.XMLEncoding;

import com.google.common.base.Function;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.service.DicomService;
import org.dcm4che2.net.service.VerificationService;
import org.nrg.dcm.scp.exceptions.DicomNetworkException;
import org.nrg.dcm.scp.exceptions.UnknownDicomHelperInstanceException;
import org.nrg.xnat.utils.NetUtils;

@Slf4j
public class DicomSCP {
    private DicomSCP(final Executor executor, final Device device, final int port, final DicomSCPManager manager) {
        _executor = executor;
        _device = device;
        _port = port;
        _manager = manager;
        setStarted(false);
    }

    public static DicomSCP create(final DicomSCPManager manager, final Executor executor, final int port) {
        return create(manager, executor, Collections.singletonList(port)).get(port);
    }

    public static Map<Integer, DicomSCP> create(final DicomSCPManager manager, final Executor executor, final List<Integer> ports) {
        if (ports == null || ports.size() == 0) {
            return null;
        }

        final Map<Integer, DicomSCP> dicomSCPs = new HashMap<>();

        for (final int port : ports) {
            if (!dicomSCPs.containsKey(port)) {
                final NetworkConnection connection = new NetworkConnection();
                connection.setPort(port);

                final Device device = new Device(DEVICE_NAME);
                device.setNetworkConnection(connection);

                dicomSCPs.put(port, new DicomSCP(executor, device, port, manager));
            }
        }
        return dicomSCPs;
    }

    public List<String> getAeTitles() {
        return new ArrayList<>(_applicationEntities.keySet());
    }

    public int getPort() {
        return _device.getNetworkConnection()[0].getPort();
    }

    public boolean isStarted() {
        return _started;
    }

    public List<String> start() throws DicomNetworkException, UnknownDicomHelperInstanceException {
        if (isStarted()) {
            log.info("The DICOM SCP on port {} has already started its configured receivers.", _port);
            return Collections.emptyList();
        }

        log.debug("Trying to start DICOM SCP receiver(s) on port {}", _port, new Exception());

        final int port = _device.getNetworkConnection()[0].getPort();
        if (port != _port) {
            log.error("The port configured for this DICOM SCP receiver on creation is {}, but the port I found in the configured network connection is {}. That's not right, so things may get weird around here.", _port, port);
        }

        if (!NetUtils.isPortAvailable(port, 3, 2)) {
            log.error("Unable to access DICOM SCP port {}. The port may be already in use, but I can't tell from the information I have now. Starting with the DICOM receiver disabled. The following AEs will be unavailable on this port: {}", _port, StringUtils.join(extractAeTitles(), ", "));
            return Collections.emptyList();
        }

        final List<DicomSCPInstance> instances = _manager.getEnabledDicomSCPInstancesByPort(_port);

        if (instances.size() == 0) {
            log.warn("No enabled DICOM SCP instances found for port {}, nothing to start", _port);
            return Collections.emptyList();
        }

        log.info("Starting DICOM SCP on {}:{}, found {} enabled DICOM SCP instances for this port", _device.getNetworkConnection()[0].getHostname(), port, instances.size());

        for (final DicomSCPInstance instance : instances) {
            addApplicationEntity(instance);
        }

        if (log.isDebugEnabled()) {
            log.debug("Application Entities: ");
            for (final NetworkApplicationEntity ae : _dicomServicesByApplicationEntity.keySet()) {
                log.debug("{}: {}", ae.getAETitle(), _dicomServicesByApplicationEntity.get(ae));
            }
        }

        final VerificationService cEcho = new VerificationService();

        for (final NetworkApplicationEntity applicationEntity : _dicomServicesByApplicationEntity.keySet()) {
            log.trace("Setting up AE {}", applicationEntity.getAETitle());
            applicationEntity.register(cEcho);

            final List<TransferCapability> transferCapabilities = Lists.newArrayList();
            transferCapabilities.add(new TransferCapability(VerificationSOPClass, VERIFICATION_SOP_TS, TransferCapability.SCP));

            for (final DicomService service : _dicomServicesByApplicationEntity.get(applicationEntity)) {
                log.trace("adding {}", service);
                applicationEntity.register(service);
                for (final String sopClass : service.getSopClasses()) {
                    transferCapabilities.add(new TransferCapability(sopClass, TSUIDS, TransferCapability.SCP));
                }
            }

            applicationEntity.setTransferCapability(transferCapabilities.toArray(new TransferCapability[0]));
        }

        final Set<NetworkApplicationEntity> applicationEntities = _dicomServicesByApplicationEntity.keySet();
        _device.setNetworkApplicationEntity(applicationEntities.toArray(new NetworkApplicationEntity[0]));
        try {
            _device.startListening(_executor);
        } catch (IOException e) {
            throw new DicomNetworkException(e);
        }

        setStarted(true);

        return Lists.transform(getAeTitles(), new Function<String, String>() {
            @Override
            public String apply(final String aeTitle) {
                return aeTitle + ":" + port;
            }
        });
    }

    public List<String> stop() {
        log.info("Stopping DICOM SCP");
        if (!isStarted()) {
            return Collections.emptyList();
        }

        _device.stopListening();

        final List<String> aeTitles = new ArrayList<>();
        for (final NetworkApplicationEntity applicationEntity : _dicomServicesByApplicationEntity.keySet()) {
            for (final DicomService service : _dicomServicesByApplicationEntity.get(applicationEntity)) {
                applicationEntity.unregister(service);
            }
            applicationEntity.setTransferCapability(new TransferCapability[0]);
            final String aeTitle = applicationEntity.getAETitle();
            aeTitles.add(aeTitle + ":" + _port);
            _applicationEntities.remove(aeTitle);
        }
        _dicomServicesByApplicationEntity.clear();

        setStarted(false);

        return aeTitles;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("DicomSCP{[").append("]: ");
        for (final Map.Entry<NetworkApplicationEntity, DicomService> ae : _dicomServicesByApplicationEntity.entries()) {
            final NetworkApplicationEntity entity = ae.getKey();
            builder.append(entity.getAETitle());
            final String hostname = entity.getNetworkConnection()[0].getHostname();
            if (StringUtils.isNotBlank(hostname)) {
                builder.append("@").append(hostname);
            }
            builder.append(":").append(_port);
        }
        return builder.append("}").toString();
    }

    private List<String> extractAeTitles() {
        return Lists.transform(_manager.getEnabledDicomSCPInstancesByPort(_port), new Function<DicomSCPInstance, String>() {
            @Override
            public String apply(final DicomSCPInstance instance) {
                return instance.getAeTitle();
            }
        });
    }

    private void setStarted(boolean started) {
        _started = started;
    }

    private void addApplicationEntity(final DicomSCPInstance instance) throws UnknownDicomHelperInstanceException {
        if (instance.getPort() != getPort()) {
            throw new RuntimeException("Port for instance " + instance.toString() + " doesn't match port for DicomSCP instance: " + getPort());
        }

        final String aeTitle = instance.getAeTitle();
        if (StringUtils.isBlank(aeTitle)) {
            throw new IllegalArgumentException("Can only add service to named AE");
        }
        if (_applicationEntities.containsKey(aeTitle)) {
            throw new RuntimeException("There's already a DICOM SCP receiver running at " + instance.toString());
        }

        _applicationEntities.put(aeTitle, new NetworkApplicationEntity() {{
            setNetworkConnection(_device.getNetworkConnection());
            setAssociationAcceptor(true);
            setAETitle(aeTitle);
        }});

        _dicomServicesByApplicationEntity.put(_applicationEntities.get(aeTitle),
                                              new CStoreService.Specifier(aeTitle,
                                                                          _manager.getUserProvider(),
                                                                          _manager.getImporter(),
                                                                          _manager.getDicomObjectIdentifier(instance.getIdentifier()),
                                                                          _manager.getDicomFileNamer(instance.getFileNamer()), _manager)
                                                  .build());
    }

    static final String DEVICE_NAME = "XNAT_DICOM";

    // Verification service can only use LE encoding
    private static final String[] VERIFICATION_SOP_TS = {ImplicitVRLittleEndian, ExplicitVRLittleEndian};

    // Accept just about anything. Some of these haven't been tested and
    // might not actually work correctly (e.g., XML encoding); some probably
    // can be received but will give the XNAT processing pipeline fits
    // (e.g., anything compressed).
    private static final String[] TSUIDS = {ExplicitVRLittleEndian,
                                            ExplicitVRBigEndian, ImplicitVRLittleEndian, JPEGBaseline1,
                                            JPEGExtended24, JPEGLosslessNonHierarchical14, JPEGLossless,
                                            JPEGLSLossless, JPEGLSLossyNearLossless, JPEG2000LosslessOnly,
                                            JPEG2000, JPEG2000Part2MultiComponentLosslessOnly,
                                            JPEG2000Part2MultiComponent, JPIPReferenced, JPIPReferencedDeflate,
                                            MPEG2, RLELossless, RFC2557MIMEEncapsulation, XMLEncoding};

    private boolean _started;

    private final Executor        _executor;
    private final Device          _device;
    private final int             _port;
    private final DicomSCPManager _manager;

    private final Map<String, NetworkApplicationEntity>            _applicationEntities              = new HashMap<>();
    private final Multimap<NetworkApplicationEntity, DicomService> _dicomServicesByApplicationEntity = Multimaps.synchronizedSetMultimap(LinkedHashMultimap.<NetworkApplicationEntity, DicomService>create());
}
