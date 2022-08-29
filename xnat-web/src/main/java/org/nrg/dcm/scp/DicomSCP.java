package org.nrg.dcm.scp;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.net.*;
import org.dcm4che2.net.service.DicomService;
import org.dcm4che2.net.service.VerificationService;
import org.nrg.dcm.scp.exceptions.DicomNetworkException;
import org.nrg.dcm.scp.exceptions.UnknownDicomHelperInstanceException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xnat.utils.NetUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PROTECTED;
import static org.dcm4che2.data.UID.*;

@Getter(PROTECTED)
@Accessors(prefix = "_")
@Slf4j
public class DicomSCP {
    private DicomSCP(final Device device, final int port, final DicomSCPManager manager) {
        if (port != device.getNetworkConnection()[0].getPort()) {
            throw new NrgServiceRuntimeException("The port configured for this DICOM SCP receiver on creation is " + port + ", but the port I found in the configured network connection is " + device.getNetworkConnection()[0].getPort() + ". That's not right, so things may get weird around here.");
        }

        _executor = manager.getExecutor();
        _device = device;
        _port = port;
        _manager = manager;
        setStarted(false);
    }

    public static DicomSCP create(final DicomSCPManager manager, final int port) {
        return create(manager, Collections.singletonList(port)).get(port);
    }

    public static Map<Integer, DicomSCP> create(final DicomSCPManager manager, final List<Integer> ports) {
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

                dicomSCPs.put(port, new DicomSCP(device, port, manager));
            }
        }
        return dicomSCPs;
    }

    public List<String> getAeTitles() {
        return new ArrayList<>(getApplicationEntities().keySet());
    }

    public int getPort() {
        return getDevice().getNetworkConnection()[0].getPort();
    }

    public boolean isStarted() {
        final boolean hasSocket = isNetworkStarted();
        final boolean started   = getStarted().get();
        if (hasSocket ^ started) {
            if (started) {
                log.warn("The DICOM SCP receiver on port {} thinks it's started, but the network connection doesn't have a server socket configured (i.e. it's not actually started). Returning the status set in the DICOM SCP object, but this is probably wrong.", getPort());
            } else {
                log.warn("The DICOM SCP receiver on port {} thinks it's not started, but the network connection has a server socket configured (i.e. it's actually started and listening). Returning the status set in the DICOM SCP object, but this is probably wrong.", getPort());
            }
        }
        return started;
    }

    public List<String> start() throws DicomNetworkException, UnknownDicomHelperInstanceException {
        if (isStarted()) {
            log.warn("The DICOM SCP on port {} has already started its configured receivers.", getPort());
            return Collections.emptyList();
        }

        final List<DicomSCPInstance> instances = getManager().getEnabledDicomSCPInstancesByPort(getPort());

        if (instances.size() == 0) {
            log.warn("No enabled DICOM SCP instances found for port {}, nothing to start", getPort());
            return Collections.emptyList();
        }

        log.debug("Trying to start DICOM SCP receiver(s) on port {}", getPort());

        if (!NetUtils.isPortAvailable(getPort(), 3, 2)) {
            log.error("Unable to access DICOM SCP port {}. The port may be already in use, but I can't tell from the information I have now. Starting with the DICOM receiver disabled. The following AEs will be unavailable on this port: {}", getPort(), StringUtils.join(extractAeTitles(), ", "));
            return Collections.emptyList();
        }

        try {
            final InetAddress localHost = InetAddress.getLocalHost();
            log.info("Starting DICOM SCP on {}{}:{}, found {} enabled DICOM SCP instances for this port", StringUtils.defaultIfBlank(getDevice().getNetworkConnection()[0].getHostname(), localHost.getHostName()), InetAddress.getByAddress(localHost.getAddress()), getPort(), instances.size());
        } catch (UnknownHostException e) {
            log.warn("Got an error retrieving localhost via InetAddress.getLocalhost()", e);
        }

        for (final DicomSCPInstance instance : instances) {
            log.debug("Adding DICOM SCP instance {}: {}", instance.getId(), instance);
            addApplicationEntity(instance);
        }

        log.debug("DICOM SCP receiver on port {} has the following {} application entities: {}", getPort(), getAeTitles().size(), StringUtils.join(getAeTitles(), ", "));
        final VerificationService cEcho = new VerificationService();

        final Set<NetworkApplicationEntity> applicationEntities = getDicomServicesByApplicationEntity().keySet();
        for (final NetworkApplicationEntity applicationEntity : applicationEntities) {
            log.trace("Setting up AE {}", applicationEntity.getAETitle());
            applicationEntity.register(cEcho);

            final List<TransferCapability> transferCapabilities = new ArrayList<>();
            transferCapabilities.add(new TransferCapability(VerificationSOPClass, VERIFICATION_SOP_TS, TransferCapability.SCP));

            for (final DicomService service : getDicomServicesByApplicationEntity().get(applicationEntity)) {
                log.trace("Adding service {}", service);
                applicationEntity.register(service);
                for (final String sopClass : service.getSopClasses()) {
                    transferCapabilities.add(new TransferCapability(sopClass, TSUIDS, TransferCapability.SCP));
                }
            }

            applicationEntity.setTransferCapability(transferCapabilities.toArray(new TransferCapability[0]));
        }

        getDevice().setNetworkApplicationEntity(applicationEntities.toArray(new NetworkApplicationEntity[0]));
        log.info("Starting DICOM SCP on port {} with {} application entities", getPort(), applicationEntities.size());

        try {
            getDevice().startListening(getExecutor());
        } catch (IOException e) {
            throw new DicomNetworkException(e);
        }

        setStarted(true);
        log.debug("Completed starting DICOM SCP on port {}, the network socket status is: {}", getPort(), isNetworkStarted());

        return getAeTitles();
    }

    public List<String> stop() {
        if (!isStarted()) {
            log.warn("Got request to stop DICOM SCP on port {} but it doesn't think it's started.", getPort());
            return Collections.emptyList();
        }

        log.info("Stopping DICOM SCP on port {}", getPort());
        getDevice().stopListening();

        final List<String> aeTitles = new ArrayList<>();
        for (final NetworkApplicationEntity applicationEntity : getDicomServicesByApplicationEntity().keySet()) {
            final String aeTitle = applicationEntity.getAETitle();
            log.debug("Removing application entity {} on port {}", aeTitle, getPort());
            aeTitles.add(aeTitle);
            for (final DicomService service : getDicomServicesByApplicationEntity().get(applicationEntity)) {
                applicationEntity.unregister(service);
            }
            applicationEntity.setTransferCapability(new TransferCapability[0]);
            getApplicationEntities().remove(aeTitle);
        }
        getDicomServicesByApplicationEntity().clear();

        setStarted(false);
        log.debug("Completed stopping DICOM SCP on port {}, the network socket status is: {}", getPort(), isNetworkStarted());
        if (!getApplicationEntities().isEmpty()) {
            log.warn("I tried to remove all application entities from DICOM SCP on port {}, but have the following leftovers: {}", getPort(), StringUtils.join(getApplicationEntities().keySet(), ", "));
        }

        return aeTitles;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("DicomSCP{[").append("]: ");
        for (final NetworkApplicationEntity applicationEntity : getDicomServicesByApplicationEntity().keySet()) {
            builder.append(applicationEntity.getAETitle());
            final String hostname = applicationEntity.getNetworkConnection()[0].getHostname();
            if (StringUtils.isNotBlank(hostname)) {
                builder.append("@").append(hostname);
            }
            builder.append(":").append(getPort());
        }
        return builder.append("}").toString();
    }

    private List<String> extractAeTitles() {
        return getManager().getEnabledDicomSCPInstancesByPort(getPort()).stream().map(DicomSCPInstance::getAeTitle).collect(Collectors.toList());
    }

    private void setStarted(final boolean started) {
        getStarted().set(started);
    }

    private boolean isNetworkStarted() {
        return _device.getNetworkConnection()[0].getServer() != null;
    }

    private void addApplicationEntity(final DicomSCPInstance instance) {
        if (instance.getPort() != getPort()) {
            throw new RuntimeException("Port for instance " + instance.getLabel() + " doesn't match port for DicomSCP instance: " + getPort());
        }

        final String aeTitle = instance.getAeTitle();
        final int port = getPort();
        if (StringUtils.isBlank(aeTitle)) {
            throw new IllegalArgumentException("Can only add service to named AE");
        }
        if (getApplicationEntities().containsKey(aeTitle)) {
            throw new RuntimeException("There's already a DICOM SCP receiver running at " + instance.getLabel());
        }

        final String fileNamer  = instance.getFileNamer();
        final String identifier = instance.getIdentifier();
        log.debug("Adding application entity \"{}\" with identifier \"{}\" and file namer \"{}\" to DICOM SCP on port {}", aeTitle, identifier, fileNamer, getPort());

        final NetworkApplicationEntity applicationEntity = createApplicationEntity(aeTitle, instance);

        getApplicationEntities().put(aeTitle, applicationEntity);
        getDicomServicesByApplicationEntity().put( applicationEntity,
                new CStoreService.Specifier( aeTitle,
                        _manager.getUserProvider(),
                        _manager.getDicomObjectIdentifier( aeTitle, port),
                        _manager)
                        .build());
    }

    @Nonnull
    private NetworkApplicationEntity createApplicationEntity(final String aeTitle, final DicomSCPInstance instance) {
        final XnatApplicationEntity applicationEntity = new XnatApplicationEntity();
        applicationEntity.setNetworkConnection(getDevice().getNetworkConnection());
        applicationEntity.setAssociationAcceptor(true);
        applicationEntity.setAETitle(aeTitle);
        applicationEntity.setWhitelist(instance.getWhitelist());
        applicationEntity.setWhitelistEnabled(instance.isWhitelistEnabled());
        return applicationEntity;
    }

    // Accept just about anything. Some of these haven't been tested and
    // might not actually work correctly (e.g., XML encoding); some probably
    // can be received but will give the XNAT processing pipeline fits
    // (e.g., anything compressed).
    private static final String[] TSUIDS              = {ExplicitVRLittleEndian,
                                                         ExplicitVRBigEndian, ImplicitVRLittleEndian, JPEGBaseline1,
                                                         JPEGExtended24, JPEGLosslessNonHierarchical14, JPEGLossless,
                                                         JPEGLSLossless, JPEGLSLossyNearLossless, JPEG2000LosslessOnly,
                                                         JPEG2000, JPEG2000Part2MultiComponentLosslessOnly,
                                                         JPEG2000Part2MultiComponent, JPIPReferenced, JPIPReferencedDeflate,
                                                         MPEG2, RLELossless, RFC2557MIMEEncapsulation, XMLEncoding};
    private static final String[] VERIFICATION_SOP_TS = {ImplicitVRLittleEndian, ExplicitVRLittleEndian}; // Verification service can only use LE encoding
    private static final String   DEVICE_NAME         = "XNAT_DICOM";

    private final Executor        _executor;
    private final Device          _device;
    private final int             _port;
    private final DicomSCPManager _manager;

    private final Map<String, NetworkApplicationEntity>            _applicationEntities              = new HashMap<>();
    private final Multimap<NetworkApplicationEntity, DicomService> _dicomServicesByApplicationEntity = Multimaps.synchronizedSetMultimap(LinkedHashMultimap.create());
    private final AtomicBoolean                                    _started                          = new AtomicBoolean();
}
