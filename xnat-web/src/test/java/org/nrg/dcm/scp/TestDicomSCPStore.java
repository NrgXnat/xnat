package org.nrg.dcm.scp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.dcm.scp.services.DicomSCPInstanceService;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.DicomObjectIdentifier;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Two threads can change the DICOM SCP receivers at once. At startup, the restlet servlet starts the enabled receivers
 * while an initialization task saves the default receiver in a transaction the servlet can't see yet. A receiver must
 * never be left listening on a port the store doesn't track, and a receiver saved in one transaction must still be
 * running once that transaction commits.
 */
public class TestDicomSCPStore {
    private static final String AE_TITLE = "XNAT";

    // What a thread's transaction can see of the receiver: everything once it's committed, or its own uncommitted save.
    private final AtomicBoolean       _committed = new AtomicBoolean(true);
    private final List<DicomSCPStore> _stores    = new ArrayList<>();

    private volatile Thread         _savingThread;
    private int                     _port;
    private DicomSCPInstance        _instance;
    private DicomSCPInstanceService _service;
    private DicomSCPManager         _manager;

    @Before
    public void setUp() throws IOException {
        _port = freePort();
        _instance = DicomSCPInstance.create(AE_TITLE, _port, "dicomObjectIdentifier");

        _service = mock(DicomSCPInstanceService.class);
        when(_service.getPortsWithEnabledInstances()).thenAnswer(invocation -> visible() ? Set.of(_port) : Set.of());
        when(_service.findAllByPort(_port)).thenAnswer(invocation -> visible() ? List.of(_instance) : List.of());
        when(_service.findByAETitleAndPort(AE_TITLE, _port)).thenAnswer(invocation -> visible() ? Optional.of(_instance) : Optional.empty());

        final ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBean(DicomFileNamer.class)).thenReturn(mock(DicomFileNamer.class));
        final SiteConfigPreferences preferences = mock(SiteConfigPreferences.class);
        when(preferences.isEnableDicomReceiver()).thenReturn(true);
        @SuppressWarnings("unchecked") final DicomObjectIdentifier<XnatProjectdata> identifier = mock(DicomObjectIdentifier.class);

        _manager = new DicomSCPManager(new DicomScpExecutor(Executors.newCachedThreadPool()), _service, mock(XnatUserProvider.class),
                                       context, mock(PlatformTransactionManager.class), preferences, identifier, Map.of("dicomObjectIdentifier", identifier));
    }

    @After
    public void tearDown() throws Exception {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        _manager.stop();
        for (final DicomSCPStore store : _stores) {
            store.stopAll();
        }
    }

    /**
     * A cycle that runs while another thread is starting a receiver waits for the start to finish. Before, the cycle
     * dropped the receiver while it was still starting, and the receiver then bound the port with nothing tracking it.
     */
    @Test
    public void cycleWaitsForAStartInProgress() throws Exception {
        final CountDownLatch starting = new CountDownLatch(1);
        final CountDownLatch release  = new CountDownLatch(1);
        final AtomicBoolean  first    = new AtomicBoolean(true);
        when(_service.findAllByPort(_port)).thenAnswer(invocation -> {
            // Hold the first start just before it binds the port.
            if (first.getAndSet(false)) {
                starting.countDown();
                release.await(10, SECONDS);
            }
            return List.of(_instance);
        });
        when(_service.findByAETitleAndPort(AE_TITLE, _port)).thenReturn(Optional.of(_instance));
        // The cycling thread can't see the receiver being started, so it treats the port as disabled.
        _committed.set(false);

        final DicomSCPStore   store   = newStore();
        final ExecutorService threads = Executors.newFixedThreadPool(2);
        try {
            final Future<?> start = threads.submit(() -> store.start(_port));
            assertTrue("the start never reached the port lookup", starting.await(10, SECONDS));

            final Future<?> cycle = threads.submit(() -> store.cycle(Set.of()));
            try {
                cycle.get(1, SECONDS);
            } catch (TimeoutException expected) {
                // The cycle is waiting for the start to finish.
            }
            release.countDown();
            start.get(10, SECONDS);
            cycle.get(10, SECONDS);

            final DicomSCP tracked = store.get(_port);
            assertEquals("the port should be listening exactly when the store tracks a started receiver on it",
                         tracked != null && tracked.isStarted(), isListening(_port));
        } finally {
            release.countDown();
            threads.shutdownNow();
        }
    }

    /**
     * A receiver saved in a transaction starts once the transaction commits, so a start on another thread that can't
     * see the uncommitted receiver can't stop it. Before, the save started the receiver inside its transaction and the
     * other thread's start stopped it again.
     */
    @Test
    public void startWhileASaveIsUncommittedLeavesTheReceiverRunning() throws Exception {
        _committed.set(false);
        _savingThread = Thread.currentThread();
        TransactionSynchronizationManager.initSynchronization();

        _manager.saveDicomSCPInstance(_instance);

        final ExecutorService servlet = Executors.newSingleThreadExecutor();
        try {
            servlet.submit(() -> _manager.start()).get(30, SECONDS);
        } finally {
            servlet.shutdownNow();
        }

        _committed.set(true);
        for (final TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }
        assertTrue(isListening(_port));
    }

    /**
     * A receiver starts from the definitions it read, so a change committed while it starts can't make it fail.
     */
    @Test
    public void startUsesTheReceiversItRead() throws Exception {
        // As if the receiver was renamed after the start read it.
        when(_service.findByAETitleAndPort(AE_TITLE, _port)).thenReturn(Optional.empty());
        newStore().start(_port);
        assertTrue(isListening(_port));
    }

    /**
     * Outside a transaction, saving a receiver starts it right away.
     */
    @Test
    public void saveOutsideATransactionStartsTheReceiver() throws Exception {
        _manager.saveDicomSCPInstance(_instance);
        assertTrue(isListening(_port));
    }

    private boolean visible() {
        return _committed.get() || Thread.currentThread() == _savingThread;
    }

    private DicomSCPStore newStore() {
        final DicomSCPStore store = new DicomSCPStore(_manager);
        _stores.add(store);
        return store;
    }

    private static int freePort() throws IOException {
        try (final ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static boolean isListening(final int port) {
        try (final Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 1000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
