package org.nrg.dcm.scp;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.dcm.scp.services.DicomSCPInstanceService;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.DicomObjectIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Receiver changes cycle their ports after their transactions commit. These run against Hibernate, since what a cycle
 * reads depends on which session it reads through.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestDicomSCPInstanceAuditingConfig.class)
public class TestDicomSCPManagerCommits {
    private static final String IDENTIFIER = "dicomObjectIdentifier";

    @Autowired
    public void setDicomSCPInstanceService(final DicomSCPInstanceService service) {
        _service = service;
    }

    @Autowired
    public void setTransactionManager(final PlatformTransactionManager transactionManager) {
        _transactionManager = transactionManager;
    }

    @Before
    public void setUp() throws IOException {
        try (final ServerSocket socket = new ServerSocket(0); final ServerSocket other = new ServerSocket(0)) {
            _port = socket.getLocalPort();
            _otherPort = other.getLocalPort();
        }

        final ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBean(DicomFileNamer.class)).thenReturn(mock(DicomFileNamer.class));
        final SiteConfigPreferences preferences = mock(SiteConfigPreferences.class);
        when(preferences.isEnableDicomReceiver()).thenReturn(true);
        @SuppressWarnings("unchecked") final DicomObjectIdentifier<XnatProjectdata> identifier = mock(DicomObjectIdentifier.class);

        _manager = new DicomSCPManager(new DicomScpExecutor(Executors.newCachedThreadPool()), _service, mock(XnatUserProvider.class),
                                       context, _transactionManager, preferences, identifier, Map.of(IDENTIFIER, identifier));
    }

    @After
    public void tearDown() throws Exception {
        _manager.stop();
        inTransaction(() -> {
            _service.findAllByPort(_port).forEach(_service::delete);
            _service.findAllByPort(_otherPort).forEach(_service::delete);
            return null;
        });
    }

    /**
     * Disabling one of two receivers on a port leaves only the other one running.
     */
    @Test
    public void disablingOneOfTwoReceiversOnAPortStopsOnlyThatOne() throws Exception {
        final DicomSCPInstance first = save("FIRST");
        save("SECOND");

        inTransaction(() -> _manager.disableDicomSCPInstance((int) first.getId()));

        assertThat(_manager.stop()).extracting(Triple::getLeft).containsExactly("SECOND");
    }

    /**
     * Moving a receiver to another port takes it off its old port, where another receiver keeps running.
     */
    @Test
    public void movingAReceiverToAnotherPortTakesItOffTheOldPort() throws Exception {
        final DicomSCPInstance first = save("FIRST");
        save("SECOND");

        // As the REST API's update does: change a detached copy of the receiver, then update with it.
        final DicomSCPInstance moved = inTransaction(() -> _manager.findById(first.getId()));
        moved.setPort(_otherPort);
        inTransaction(() -> _manager.update(moved, false));

        assertThat(_manager.stop()).containsExactlyInAnyOrder(Triple.of("SECOND", _port, false), Triple.of("FIRST", _otherPort, false));
    }

    /**
     * A cycle after a commit reads the receivers as they are when it runs, not as its transaction left them. Here one
     * change commits and waits before cycling while a second change renames the receiver and cycles. The first cycle
     * then has to start the receiver under its new AE title.
     */
    @Test
    public void cycleAfterCommitReadsChangesCommittedSince() throws Exception {
        final long id = save("BEFORE").getId();

        final CountDownLatch  paused  = new CountDownLatch(1);
        final CountDownLatch  resume  = new CountDownLatch(1);
        final ExecutorService threads = Executors.newSingleThreadExecutor();
        try {
            final Future<?> first = threads.submit(() -> inTransaction(() -> {
                // Registered before the manager's cycle, so it holds that cycle until the second change is done.
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        paused.countDown();
                        try {
                            resume.await(30, SECONDS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                });
                final DicomSCPInstance instance = _service.retrieve(id);
                instance.setAnonymizationEnabled(false);
                return _manager.updateDicomSCPInstance(instance);
            }));
            assertThat(paused.await(30, SECONDS)).isTrue();

            inTransaction(() -> {
                final DicomSCPInstance instance = _service.retrieve(id);
                instance.setAeTitle("AFTER");
                return _manager.updateDicomSCPInstance(instance);
            });

            resume.countDown();
            first.get(30, SECONDS);
        } finally {
            resume.countDown();
            threads.shutdownNow();
        }

        assertThat(_manager.stop()).extracting(Triple::getLeft).containsExactly("AFTER");
    }

    private DicomSCPInstance save(final String aeTitle) {
        return inTransaction(() -> _manager.saveDicomSCPInstance(DicomSCPInstance.create(aeTitle, _port, IDENTIFIER)));
    }

    private <T> T inTransaction(final Callable<T> work) {
        return new TransactionTemplate(_transactionManager).execute(status -> {
            try {
                return work.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private DicomSCPInstanceService    _service;
    private PlatformTransactionManager _transactionManager;
    private int                        _port;
    private int                        _otherPort;
    private DicomSCPManager            _manager;
}
