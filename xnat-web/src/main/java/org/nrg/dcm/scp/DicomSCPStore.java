package org.nrg.dcm.scp;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.nrg.dcm.scp.exceptions.DicomNetworkException;
import org.nrg.dcm.scp.exceptions.UnknownDicomHelperInstanceException;

import javax.annotation.Nonnull;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tracks the DICOM SCP receiver on each port. Callers on different threads (startup, initialization tasks, the REST
 * API) start and stop receivers concurrently, so every method holds this store's lock: a receiver dropped from the
 * store while it's still starting would go on to bind its port with nothing left to stop it.
 */
@Slf4j
public class DicomSCPStore {
    private final Map<Integer, DicomSCP> _dicomSCPs = new HashMap<>();

    private final DicomSCPManager _manager;

    public DicomSCPStore(final DicomSCPManager manager) {
        _manager = manager;
    }

    public synchronized Set<Integer> ports() {
        return ImmutableSet.copyOf(_dicomSCPs.keySet());
    }

    public synchronized Collection<? extends DicomSCP> get() {
        return new ArrayList<>(_dicomSCPs.values());
    }

    public synchronized List<Triple<String, Integer, Boolean>> start(final Integer port) throws DicomNetworkException, UnknownDicomHelperInstanceException, GeneralSecurityException {
        if (_dicomSCPs.containsKey(port)) {
            log.debug("Request to start DICOM SCP on port {}, but that already exists. Removing and stopping existing instance.", port);
            final DicomSCP dicomSCP = _dicomSCPs.remove(port);
            dicomSCP.stop();
        }
        final DicomSCP dicomSCP = DicomSCP.create(_manager, port);
        _dicomSCPs.put(port, dicomSCP);
        final List<String> aeTitles = dicomSCP.start();
        if (log.isDebugEnabled()) {
            log.debug("Created and started new DICOM SCP on port {}, got {} AE titles for this port: {}", port, aeTitles.size(), aeTitles.isEmpty() ? "<nothing>" : String.join(", ", aeTitles));
        }
        return convertReceivers(aeTitles, port, true);
    }

    @Nonnull
    public synchronized DicomSCP get(final int port) {
        return _dicomSCPs.get(port);
    }

    public synchronized List<Triple<String, Integer, Boolean>> stop(final int port) {
        if (!_dicomSCPs.containsKey(port)) {
            log.debug("Request to stop DICOM SCP on port {}, but that doesn't exist.", port);
            return Collections.emptyList();
        }
        final DicomSCP     dicomSCP  = _dicomSCPs.remove(port);
        final boolean      isStarted = dicomSCP.isStarted();
        final List<String> aeTitles  = isStarted ? dicomSCP.stop() : Collections.emptyList();
        if (isStarted) {
            log.debug("Request to stop DICOM SCP on port {}, removed instance, stopped it, included {} AE titles: {}", port, aeTitles.size(), StringUtils.join(aeTitles, ", "));
        } else {
            log.debug("Request to stop DICOM SCP on port {}, removed instance, but it wasn't started, just disposing of it", port);
        }
        return isStarted ? convertReceivers(aeTitles, port, false) : Collections.emptyList();
    }

    public synchronized List<Triple<String, Integer, Boolean>> cycle(final Set<Integer> updated) throws DicomNetworkException, UnknownDicomHelperInstanceException, GeneralSecurityException {
        final List<Triple<String, Integer, Boolean>> results = new ArrayList<>();

        final Set<Integer> disabled = new HashSet<>(Sets.difference(ports(), _manager.getPortsWithEnabledInstances()));
        if (!disabled.isEmpty()) {
            log.debug("Found {} DICOM SCPs without enabled DICOM SCP instances, stopping on ports: {}", disabled.size(), disabled);
            results.addAll(stop(disabled));
        }
        final Set<Integer> modified = new HashSet<>(Sets.difference(updated, disabled));
        // A receiver moved to another port is still running on its old port, which the change didn't name.
        for (final int port : ports()) {
            if (!modified.contains(port) && runsOtherReceivers(port)) {
                log.debug("The DICOM SCP on port {} runs AE titles other than the port's enabled receivers, cycling it too", port);
                modified.add(port);
            }
        }
        log.debug("Got request to cycle {} updated DICOM SCPs, cycling ports: {}", modified.size(), modified);
        results.addAll(start(modified));
        return results;
    }

    public synchronized List<Triple<String, Integer, Boolean>> stopAll() throws DicomNetworkException, UnknownDicomHelperInstanceException, GeneralSecurityException {
        return stop(ports());
    }

    /**
     * Whether the receiver started on this port runs AE titles other than the port's enabled receivers.
     */
    private boolean runsOtherReceivers(final int port) {
        final DicomSCP dicomSCP = _dicomSCPs.get(port);
        if (!dicomSCP.isStarted()) {
            return false;
        }
        final Set<String> enabled = _manager.getEnabledDicomSCPInstancesByPort(port).stream().map(DicomSCPInstance::getAeTitle).collect(Collectors.toSet());
        return !enabled.equals(new HashSet<>(dicomSCP.getAeTitles()));
    }

    @Nonnull
    private List<Triple<String, Integer, Boolean>> start(final Set<Integer> ports) throws DicomNetworkException, UnknownDicomHelperInstanceException, GeneralSecurityException {
        return toggle(ports, true);
    }

    @Nonnull
    private List<Triple<String, Integer, Boolean>> stop(final Set<Integer> ports) throws DicomNetworkException, UnknownDicomHelperInstanceException, GeneralSecurityException {
        return toggle(ports, false);
    }

    @Nonnull
    private List<Triple<String, Integer, Boolean>> toggle(final Set<Integer> ports, final boolean enable) throws DicomNetworkException, UnknownDicomHelperInstanceException, GeneralSecurityException {
        log.debug("Got request to {} operations on {} ports: {}", enable ? "start" : "stop", ports.size(), StringUtils.join(ports, ", "));
        final List<Triple<String, Integer, Boolean>> results = new ArrayList<>();
        for (final int port : ports) {
            log.info("{} operations on port {}", enable ? "Starting" : "Stopping", port);
            final List<Triple<String, Integer, Boolean>> titles = enable ? start(port) : stop(port);
            if (titles.isEmpty()) {
                log.warn("Tried to {} operations on port {}, but no AE titles were returned.", enable ? "start" : "stop", port);
            } else {
                log.debug("Got {} AE titles when {} port {}: {}", titles.size(), enable ? "starting" : "stopping", port, StringUtils.join(titles, ", "));
                results.addAll(titles);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("{} operations on ports {}, got {} total AEs in response: {}", enable ? "Started" : "Stopped", ports, results.size(), results.stream().map(result -> DicomSCPInstance.formatDicomSCPInstanceKey(result.getLeft(), result.getMiddle())).collect(Collectors.joining(", ")));
        }
        return results;
    }

    private static List<Triple<String, Integer, Boolean>> convertReceivers(final List<String> aeTitles, final Integer port, final boolean enabled) {
        return aeTitles.stream().map(aeTitle -> Triple.of(aeTitle, port, enabled)).collect(Collectors.toList());
    }
}
