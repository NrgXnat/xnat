package org.nrg.dcm.scp;

import com.google.common.base.Joiner;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;

import static org.dcm4che2.data.UID.*;

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
            logger.info("The DICOM SCP on port {} has already started its configured receivers.", _port);
            return Collections.emptyList();
        }

        if (!NetUtils.isPortAvailable(_device.getNetworkConnection()[0].getPort())) {
            logger.error("DICOM SCP port {} is in use; starting with the DICOM receiver disabled. The following AEs will be unavailable on this port: {}", _port, Joiner.on(", ").join(getAeTitles()));
            return Collections.emptyList();
        }

        final List<DicomSCPInstance> instances = _manager.getEnabledDicomSCPInstancesByPort(_port);

        if (instances.size() == 0) {
            logger.warn("No enabled DICOM SCP instances found for port {}, nothing to start", _port);
            return Collections.emptyList();
        }

        logger.info("Starting DICOM SCP on {}:{}, found {} enabled DICOM SCP instances for this port", _device.getNetworkConnection()[0].getHostname(), _device.getNetworkConnection()[0].getPort(), instances.size());

        for (final DicomSCPInstance instance : instances) {
            addApplicationEntity(instance);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Application Entities: ");
            for (final NetworkApplicationEntity ae : _dicomServicesByApplicationEntity.keySet()) {
                logger.debug("{}: {}", ae.getAETitle(), _dicomServicesByApplicationEntity.get(ae));
            }
        }

        final VerificationService cEcho = new VerificationService();

        final List<String> aeTitles = new ArrayList<>();
        for (final NetworkApplicationEntity applicationEntity : _dicomServicesByApplicationEntity.keySet()) {
            logger.trace("Setting up AE {}", applicationEntity.getAETitle());
            applicationEntity.register(cEcho);

            final List<TransferCapability> transferCapabilities = Lists.newArrayList();
            transferCapabilities.add(new TransferCapability(VerificationSOPClass, VERIFICATION_SOP_TS, TransferCapability.SCP));

            for (final DicomService service : _dicomServicesByApplicationEntity.get(applicationEntity)) {
                logger.trace("adding {}", service);
                applicationEntity.register(service);
                for (final String sopClass : service.getSopClasses()) {
                    transferCapabilities.add(new TransferCapability(sopClass, TSUIDS, TransferCapability.SCP));
                }
            }

            applicationEntity.setTransferCapability(transferCapabilities.toArray(new TransferCapability[transferCapabilities.size()]));
            aeTitles.add(applicationEntity.getAETitle() + ":" + _port);
        }

        final Set<NetworkApplicationEntity> applicationEntities = _dicomServicesByApplicationEntity.keySet();
        _device.setNetworkApplicationEntity(applicationEntities.toArray(new NetworkApplicationEntity[applicationEntities.size()]));
        try {
            _device.startListening(_executor);
        } catch (IOException e) {
            throw new DicomNetworkException(e);
        }

        setStarted(true);

        return aeTitles;
    }

    public List<String> stop() {
        logger.info("stopping DICOM SCP");
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
                                                                          _manager.getDicomObjectIdentifier(instance.getIdentifier()),
                                                                          _manager.getDicomFileNamer(instance.getFileNamer()))
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

    private static final Logger logger = LoggerFactory.getLogger(DicomSCP.class);

    private boolean _started;

    private final Executor        _executor;
    private final Device          _device;
    private final int             _port;
    private final DicomSCPManager _manager;

    private final Map<String, NetworkApplicationEntity>            _applicationEntities              = new HashMap<>();
    private final Multimap<NetworkApplicationEntity, DicomService> _dicomServicesByApplicationEntity = Multimaps.synchronizedSetMultimap(LinkedHashMultimap.<NetworkApplicationEntity, DicomService>create());
}
