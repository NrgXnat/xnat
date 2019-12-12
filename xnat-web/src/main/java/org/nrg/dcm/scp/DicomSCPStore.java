package org.nrg.dcm.scp;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.nrg.dcm.scp.exceptions.DicomNetworkException;
import org.nrg.dcm.scp.exceptions.UnknownDicomHelperInstanceException;

@Slf4j
public class DicomSCPStore {
    public DicomSCPStore(final ExecutorService executorService, final DicomSCPManager manager) {
        _executorService = executorService;
        _manager = manager;
    }

    public Set<Integer> ports() {
        return _dicomSCPs.keySet();
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
        return Lists.transform(dicomSCP.start(), new ReceiverTransform(port, true));
    }

    public DicomSCP get(final int port) {
        return _dicomSCPs.get(port);
    }

    public List<Triple<String, Integer, Boolean>> stop(final int port) {
        if (!_dicomSCPs.containsKey(port)) {
            return Collections.emptyList();
        }
        final DicomSCP dicomSCP = _dicomSCPs.remove(port);
        return dicomSCP.isStarted() ? Lists.transform(dicomSCP.stop(), new ReceiverTransform(port, false)) : Collections.<Triple<String, Integer, Boolean>>emptyList();
    }

    public List<Triple<String, Integer, Boolean>> cycle(final Set<Integer> updated) throws DicomNetworkException, UnknownDicomHelperInstanceException {
        final List<Triple<String, Integer, Boolean>> results = new ArrayList<>();

        final Set<Integer> disabled = new HashSet<>(Sets.difference(ports(), _manager.getPortsWithEnabledInstances()));
        if (!disabled.isEmpty()) {
            log.debug("Found {} DICOM SCPs without enabled DICOM SCP instances, stopping on ports: {}", disabled.size(), disabled);
            results.addAll(stop(disabled));
        }
        log.debug("Got request to cycle {} updated DICOM SCPs, stopping on ports: {}", updated.size(), updated);
        results.addAll(start(updated));
        return results;
    }

    public List<Triple<String, Integer, Boolean>> stopAll() throws DicomNetworkException, UnknownDicomHelperInstanceException {
        return stop(_dicomSCPs.keySet());
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
        final List<Triple<String, Integer, Boolean>> results = new ArrayList<>();
        for (final int port : ports) {
            final List<Triple<String, Integer, Boolean>> titles = enable ? start(port) : stop(port);
            if (titles.isEmpty()) {
                log.warn("Tried to {} operations on port {}, but no AE titles were returned.", enable ? "start" : "stop", port);
            } else {
                results.addAll(titles);
            }
        }
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

    private final Map<Integer, DicomSCP> _dicomSCPs = new HashMap<>();

    private final ExecutorService _executorService;
    private final DicomSCPManager _manager;
}
