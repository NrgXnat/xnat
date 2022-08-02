package org.nrg.dcm.scp;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.nrg.dcm.scp.exceptions.DicomNetworkException;
import org.nrg.dcm.scp.exceptions.UnknownDicomHelperInstanceException;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ExecutorService;

@Slf4j
public class DicomSCPStore {
    private final Map<Integer, DicomSCP> _dicomSCPs = new HashMap<>();
    private final ExecutorService _executorService;
    private final DicomSCPManager _manager;

    public DicomSCPStore(final ExecutorService executorService, final DicomSCPManager manager) {
        _executorService = executorService;
        _manager = manager;
    }

    public Set<Integer> ports() {
        return ImmutableSet.copyOf(_dicomSCPs.keySet());
    }

    public Collection<? extends DicomSCP> get() {
        return _dicomSCPs.values();
    }

    public List<Triple<String, Integer, Boolean>> start(final Integer port) throws DicomNetworkException, UnknownDicomHelperInstanceException {
        if (_dicomSCPs.containsKey(port)) {
            log.debug("Request to start DICOM SCP on port {}, but that already exists. Removing and stopping existing instance.", port);
            final DicomSCP dicomSCP = _dicomSCPs.remove(port);
            dicomSCP.stop();
        }
        final DicomSCP dicomSCP = DicomSCP.create(_manager, _executorService, port);
        _dicomSCPs.put(port, dicomSCP);
        final List<String> aeTitles = dicomSCP.start();
        log.debug("Created and started new DICOM SCP on port {}, got {} AE titles for this port: {}", port, aeTitles.size(), aeTitles.isEmpty() ? "<nothing>" : StringUtils.join(aeTitles, ", "));
        return Lists.transform(aeTitles, new ReceiverTransform(port, true));
    }

    public DicomSCP get(final int port) {
        return _dicomSCPs.get(port);
    }

    public List<Triple<String, Integer, Boolean>> stop(final int port) {
        if (!_dicomSCPs.containsKey(port)) {
            log.debug("Request to stop DICOM SCP on port {}, but that doesn't exist.", port);
            return Collections.emptyList();
        }
        final DicomSCP     dicomSCP  = _dicomSCPs.remove(port);
        final boolean      isStarted = dicomSCP.isStarted();
        final List<String> aeTitles  = isStarted ? dicomSCP.stop() : Collections.<String>emptyList();
        if (isStarted) {
            log.debug("Request to stop DICOM SCP on port {}, removed instance, stopped it, included {} AE titles: {}", port, aeTitles.size(), StringUtils.join(aeTitles, ", "));
        } else {
            log.debug("Request to stop DICOM SCP on port {}, removed instance, but it wasn't started, just disposing of it", port);
        }
        return isStarted ? Lists.transform(aeTitles, new ReceiverTransform(port, false)) : Collections.<Triple<String, Integer, Boolean>>emptyList();
    }

    public List<Triple<String, Integer, Boolean>> cycle(final Set<Integer> updated) throws DicomNetworkException, UnknownDicomHelperInstanceException {
        final List<Triple<String, Integer, Boolean>> results = new ArrayList<>();

        final Set<Integer> disabled = new HashSet<>(Sets.difference(ports(), _manager.getPortsWithEnabledInstances()));
        if (!disabled.isEmpty()) {
            log.debug("Found {} DICOM SCPs without enabled DICOM SCP instances, stopping on ports: {}", disabled.size(), disabled);
            results.addAll(stop(disabled));
        }
        final Set<Integer> modified = Sets.difference(updated, disabled);
        log.debug("Got request to cycle {} updated DICOM SCPs, cycling ports: {}", modified.size(), modified);
        results.addAll(start(modified));
        return results;
    }

    public List<Triple<String, Integer, Boolean>> stopAll() throws DicomNetworkException, UnknownDicomHelperInstanceException {
        return stop(ports());
    }

    @Nonnull
    private List<Triple<String, Integer, Boolean>> start(final Set<Integer> ports) throws DicomNetworkException, UnknownDicomHelperInstanceException {
        return toggle(ports, true);
    }

    @Nonnull
    private List<Triple<String, Integer, Boolean>> stop(final Set<Integer> ports) throws DicomNetworkException, UnknownDicomHelperInstanceException {
        return toggle(ports, false);
    }

    @Nonnull
    private List<Triple<String, Integer, Boolean>> toggle(final Set<Integer> ports, final boolean enable) throws DicomNetworkException, UnknownDicomHelperInstanceException {
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
        log.debug("{} operations on ports {}, got {} total AEs in response: {}", enable ? "Started" : "Stopped", ports, results.size(), StringUtils.join(Lists.transform(results, AE_TRIPLE_TO_AE_PORT), ", "));
        return results;
    }

    private static class ReceiverTransform implements Function<String, Triple<String, Integer, Boolean>> {
        ReceiverTransform(final int port, final boolean enabled) {
            _port = port;
            _enabled = enabled;
        }

        @Override
        public Triple<String, Integer, Boolean> apply(final String aeTitle) {
            return Triple.of(aeTitle, _port, _enabled);
        }

        private final int     _port;
        private final boolean _enabled;
    }

    private static final Function<Triple<String, Integer, Boolean>, String> AE_TRIPLE_TO_AE_PORT = new Function<Triple<String, Integer, Boolean>, String>() {
        @Override
        public String apply(final Triple<String, Integer, Boolean> entity) {
            return DicomSCPInstance.formatDicomSCPInstanceKey(entity.getLeft(), entity.getMiddle());
        }
    };
}
