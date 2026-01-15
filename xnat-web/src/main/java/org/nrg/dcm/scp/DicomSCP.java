package org.nrg.dcm.scp;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.WhitelistAssociationHandler;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.nrg.dcm.scp.exceptions.DicomNetworkException;
import org.nrg.dcm.scp.exceptions.UnknownDicomHelperInstanceException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xnat.utils.NetUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PROTECTED;

@Getter(PROTECTED)
@Accessors(prefix = "_")
@Slf4j
public class DicomSCP {
    private DicomSCP(final Device device, final int port, final DicomSCPManager manager) {
        if (port != device.listConnections().getFirst().getPort()) {
            throw new NrgServiceRuntimeException("The port configured for this DICOM SCP receiver on creation is " + port + ", but the port I found in the configured network connection is " + device.listConnections().getFirst().getPort() + ". That's not right, so things may get weird around here.");
        }

        _executor = manager.getExecutor();
        _device = device;
        _device.setExecutor(_executor);
        _device.setScheduledExecutor(Executors.newSingleThreadScheduledExecutor());
        _manager = manager;
        setStarted(false);
    }

    public static DicomSCP create(final DicomSCPManager manager, final int port) {
        return create(manager, Collections.singletonList(port)).get(port);
    }

    @Nonnull
    public static Map<Integer, DicomSCP> create(final DicomSCPManager manager, final List<Integer> ports) {
        if (CollectionUtils.isEmpty(ports)) {
            throw new IllegalArgumentException("You must provide at least one port to create a DICOM SCP receiver.");
        }

        final Map<Integer, DicomSCP> dicomSCPs = new HashMap<>();

        for (final int port : ports) {
            if (!dicomSCPs.containsKey(port)) {
                final Connection connection = new Connection();
                connection.setHostname("0.0.0.0");
                connection.setPort(port);

                final Device device = new Device(DEVICE_NAME);
                device.addConnection(connection);

                WhitelistAssociationHandler handler = new WhitelistAssociationHandler();
                device.setAssociationHandler(handler);

                dicomSCPs.put(port, new DicomSCP(device, port, manager));
            }
        }
        return dicomSCPs;
    }

    public List<String> getAeTitles() {
        return new ArrayList<>(getApplicationEntities().keySet());
    }

    public int getPort() {
        return getDevice().listConnections().getFirst().getPort();
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

    public List<String> start() throws DicomNetworkException, UnknownDicomHelperInstanceException, GeneralSecurityException {
        if (isStarted()) {
            log.warn("The DICOM SCP on port {} has already started its configured receivers.", getPort());
            return Collections.emptyList();
        }

        final List<DicomSCPInstance> instances = getManager().getEnabledDicomSCPInstancesByPort(getPort());

        if (CollectionUtils.isEmpty(instances)) {
            log.warn("No enabled DICOM SCP instances found for port {}, nothing to start", getPort());
            return Collections.emptyList();
        }

        if (!NetUtils.isPortAvailable(getPort(), 3, 2)) {
            log.error("Unable to access DICOM SCP port {}. The port may be already in use, but I can't tell from the information I have now. Starting with the DICOM receiver disabled. The following AEs will be unavailable on this port: {}", getPort(), StringUtils.join(extractAeTitles(), ", "));
            return Collections.emptyList();
        }

        log.debug("Trying to start {} DICOM SCP receiver(s) on port {}", instances.size(), getPort());

        try {
            final InetAddress localHost = InetAddress.getLocalHost();
            log.info("Starting DICOM SCP on {}{}:{}, found {} enabled DICOM SCP instances for this port", StringUtils.defaultIfBlank(getDevice().listConnections().getFirst().getHostname(), localHost.getHostName()), InetAddress.getByAddress(localHost.getAddress()), getPort(), instances.size());
        } catch (UnknownHostException e) {
            log.warn("Got an error retrieving localhost via InetAddress.getLocalhost()", e);
        }

        for (final DicomSCPInstance instance : instances) {
            log.debug("Adding DICOM SCP instance {}: {}", instance.getId(), instance);
            addApplicationEntity(instance);
        }

        log.debug("DICOM SCP receiver on port {} has the following {} application entities: {}", getPort(), getAeTitles().size(), StringUtils.join(getAeTitles(), ", "));
        final BasicCEchoSCP cEcho = new BasicCEchoSCP();

        final Set<ApplicationEntity> applicationEntities = getDicomServicesByApplicationEntity().keySet();

        for (final ApplicationEntity applicationEntity : applicationEntities) {
            log.trace("Setting up AE {}", applicationEntity.getAETitle());

            applicationEntity.addTransferCapability(
                    new TransferCapability(null,
                            UID.Verification,
                            TransferCapability.Role.SCP,
                            UID.ImplicitVRLittleEndian,
                            UID.ExplicitVRLittleEndian));

            DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
            serviceRegistry.addDicomService(cEcho);
            for (final BasicCStoreSCP service : getDicomServicesByApplicationEntity().get(applicationEntity)) {
                log.trace("Adding service {}", service);
                serviceRegistry.addDicomService(service);

                for (final String sopClass : service.getSOPClasses()) {
                    applicationEntity.addTransferCapability(new TransferCapability(null, sopClass, TransferCapability.Role.SCP,TSUIDS));
                }
            }
            // MERGE: This was commented out in develop. Is it necessary?
            applicationEntity.setDimseRQHandler(serviceRegistry);
        }

        for (final ApplicationEntity ae : applicationEntities) {
            getDevice().addApplicationEntity(ae);
        }

        log.info("Starting DICOM SCP on port {} with {} application entities", getPort(), applicationEntities.size());

        try {
            getDevice().bindConnections();
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
        getDevice().unbindConnections();

        final List<String> aeTitles = getDicomServicesByApplicationEntity().keySet().stream().filter(Objects::nonNull).map(applicationEntity -> {
            final String aeTitle = applicationEntity.getAETitle();
            log.debug("Removing application entity {} on port {}", aeTitle, getPort());
            applicationEntity.addTransferCapability(new TransferCapability());
            applicationEntity.setDimseRQHandler(new DicomServiceRegistry());
            getApplicationEntities().remove(aeTitle);
            getDevice().removeApplicationEntity(applicationEntity);
            return aeTitle;
        }).collect(Collectors.toList());

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
        getDicomServicesByApplicationEntity().keySet().stream().filter(Objects::nonNull).forEach(applicationEntity -> {
            builder.append(applicationEntity.getAETitle());
            final String hostname = applicationEntity.getConnections().getFirst().getHostname();
            if (StringUtils.isNotBlank(hostname)) {
                builder.append("@").append(hostname);
            }
            builder.append(":").append(getPort());
        });
        return builder.append("}").toString();
    }

    private List<String> extractAeTitles() {
        return getManager().getEnabledDicomSCPInstancesByPort(getPort()).stream().map(DicomSCPInstance::getAeTitle).collect(Collectors.toList());
    }

    private void setStarted(final boolean started) {
        getStarted().set(started);
    }

    private boolean isNetworkStarted() {
        return _device.listConnections().getFirst().isListening();
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

        final ApplicationEntity applicationEntity = createApplicationEntity(aeTitle);
        ((WhitelistAssociationHandler)getDevice().getAssociationHandler()).addWhitelist(applicationEntity.getAETitle(), instance.isWhitelistEnabled(), instance.getWhitelist());

        getApplicationEntities().put(aeTitle, applicationEntity);
        getDicomServicesByApplicationEntity().put(applicationEntity,
                new CStoreService.Specifier(aeTitle,
                        _manager.getUserProvider(),
                        _manager.getDicomObjectIdentifier(aeTitle, port),
                        _manager.getDicomFileNamer(fileNamer),
                        _manager)
                        .build());
    }

    @Nonnull
    private ApplicationEntity createApplicationEntity(final String aeTitle) {
        final ApplicationEntity applicationEntity = new ApplicationEntity(aeTitle);
        applicationEntity.addConnection(getDevice().listConnections().getFirst());
        applicationEntity.setAssociationAcceptor(true);
        return applicationEntity;
    }

    // Accept just about anything. Some of these haven't been tested and
    // might not actually work correctly (e.g., XML encoding); some probably
    // can be received but will give the XNAT processing pipeline fits
    // (e.g., anything compressed).
    private static final String[] TSUIDS              = {
            UID.ExplicitVRLittleEndian,
            UID.ExplicitVRBigEndian,
            UID.ImplicitVRLittleEndian,
            UID.JPEGBaseline8Bit,
            UID.JPEGExtended12Bit,
            UID.JPEGLossless,
            UID.JPEGLossless,
            UID.JPEGLSLossless,
            UID.JPEGLSNearLossless,
            UID.JPEG2000Lossless,
            UID.JPEG2000,
            UID.JPEG2000MCLossless,
            UID.JPEG2000MC,
            UID.JPIPReferenced,
            UID.JPIPReferencedDeflate,
            UID.MPEG2MPML,
            UID.RLELossless,
            UID.RFC2557MIMEEncapsulation,
            UID.XMLEncoding};
    private static final String   DEVICE_NAME         = "XNAT_DICOM";

    private final Executor        _executor;
    private final Device          _device;
    private final DicomSCPManager _manager;

    private final Map<String, ApplicationEntity>            _applicationEntities              = new HashMap<>();
    private final Multimap<ApplicationEntity, CStoreService> _dicomServicesByApplicationEntity = Multimaps.synchronizedSetMultimap(LinkedHashMultimap.create());
    private final AtomicBoolean                                    _started                          = new AtomicBoolean();
}
