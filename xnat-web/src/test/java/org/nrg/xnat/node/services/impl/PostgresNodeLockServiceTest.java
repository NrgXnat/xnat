/*
 * web: org.nrg.xnat.node.services.impl.PostgresNodeLockServiceTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.node.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.node.NodeLeader;
import org.nrg.framework.node.NodeLeaderListener;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.junit.Assume.assumeTrue;

/**
 * Runs {@link PostgresNodeLockService} against a Testcontainers Postgres. Each service instance opens its own lock
 * connection, so two instances in one JVM behave exactly like two nodes.
 */
@Slf4j
public class PostgresNodeLockServiceTest {
    private static final String   LOCK          = "test:leader";
    private static final Duration TICK          = Duration.ofMillis(200);
    private static final Duration CHECK_TIMEOUT = Duration.ofMillis(300);
    private static final int      MAX_TIMEOUTS  = 3;
    private static final Duration WAIT          = Duration.ofSeconds(5);

    private static PostgreSQLContainer<?> postgres;

    private final List<PostgresNodeLockService> _services = new ArrayList<>();

    @BeforeClass
    public static void startPostgres() {
        assumeTrue("Docker is not available, skipping node lock tests", DockerClientFactory.instance().isDockerAvailable());
        postgres = new PostgreSQLContainer<>("postgres:12").withDatabaseName("test").withUsername("test").withPassword("test");
        postgres.start();
    }

    @AfterClass
    public static void stopPostgres() {
        if (postgres != null) {
            postgres.stop();
        }
    }

    @Before
    public void resetDriver() {
        InstrumentedTestDriver.reset();
    }

    @After
    public void closeServices() {
        for (final PostgresNodeLockService service : _services) {
            service.close();
        }
        _services.clear();
    }

    @Test
    public void singleNodeElectsAtOnce() {
        final PostgresNodeLockService service  = node("A");
        final RecordingListener       listener = new RecordingListener();
        final NodeLeader              leader   = service.registerLeader(LOCK, listener);

        await().atMost(WAIT).until(leader::isLeader);
        await().atMost(WAIT).until(() -> listener.elected.size() == 1);
        assertThat(listener.demoted).isEmpty();
        assertThat(service.isEnabled()).isTrue();
        assertThat(service.isConnected()).isTrue();
        assertThat(lockHolders()).containsExactly(PostgresNodeLockService.APPLICATION_NAME_PREFIX + "A");
    }

    @Test
    public void secondNodeWaitsUntilLockIsFree() {
        final PostgresNodeLockService a        = node("A");
        final RecordingListener       aEvents  = new RecordingListener();
        final NodeLeader              aLeader  = a.registerLeader(LOCK, aEvents);
        await().atMost(WAIT).until(aLeader::isLeader);

        final PostgresNodeLockService b        = node("B");
        final RecordingListener       bEvents  = new RecordingListener();
        final NodeLeader              bLeader  = b.registerLeader(LOCK, bEvents);

        sleep(TICK.multipliedBy(5));
        assertThat(bLeader.isLeader()).isFalse();
        assertThat(bEvents.elected).isEmpty();
        assertThat(aLeader.isLeader()).isTrue();
    }

    @Test
    public void terminatedBackendDemotesAtOnceAndOtherNodeElects() throws SQLException {
        final PostgresNodeLockService a       = node("A");
        final RecordingListener       aEvents = new RecordingListener();
        final NodeLeader              aLeader = a.registerLeader(LOCK, aEvents);
        await().atMost(WAIT).until(aLeader::isLeader);

        final PostgresNodeLockService b       = node("B");
        final RecordingListener       bEvents = new RecordingListener();
        final NodeLeader              bLeader = b.registerLeader(LOCK, bEvents);
        sleep(TICK.multipliedBy(2));
        assertThat(bLeader.isLeader()).isFalse();

        terminateLockConnection("A");

        await().atMost(WAIT).until(() -> !aLeader.isLeader());
        await().atMost(WAIT).until(bLeader::isLeader);
        await().atMost(WAIT).until(() -> aEvents.demoted.size() == 1);
        assertThat(aEvents.demoted.get(0)).contains("lock connection lost");
        assertThat(bEvents.elected).hasSize(1);

        // A reconnects, and stays a follower because B holds the lock.
        await().atMost(WAIT).until(a::isConnected);
        sleep(TICK.multipliedBy(3));
        assertThat(aLeader.isLeader()).isFalse();
        assertThat(bLeader.isLeader()).isTrue();
        assertThat(lockHolders()).containsExactly(PostgresNodeLockService.APPLICATION_NAME_PREFIX + "B");
    }

    @Test
    public void stepDownReleasesAndWaitsOutBackoff() {
        final PostgresNodeLockService a       = node("A");
        final RecordingListener       aEvents = new RecordingListener();
        final NodeLeader              aLeader = a.registerLeader(LOCK, aEvents);
        await().atMost(WAIT).until(aLeader::isLeader);

        final PostgresNodeLockService b       = node("B");
        final RecordingListener       bEvents = new RecordingListener();
        final NodeLeader              bLeader = b.registerLeader(LOCK, bEvents);

        final Duration backoff = Duration.ofSeconds(2);
        final long     steppedDownAt = System.nanoTime();
        assertThat(aLeader.stepDown(backoff)).isTrue();
        assertThat(aLeader.isLeader()).isFalse();
        await().atMost(WAIT).until(() -> aEvents.demoted.size() == 1);
        assertThat(aEvents.demoted.get(0)).isEqualTo("stepped down");
        await().atMost(WAIT).until(bLeader::isLeader);

        // B goes away. A must not take the lock back until its backoff has passed.
        b.close();
        await().atMost(WAIT.plus(backoff)).until(aLeader::isLeader);
        final Duration waited = Duration.ofNanos(System.nanoTime() - steppedDownAt);
        assertThat(waited).isGreaterThanOrEqualTo(backoff);
        assertThat(aEvents.elected).hasSize(2);
    }

    @Test
    public void stepDownIsRefusedWhenNoOtherNodeIsConnected() {
        final PostgresNodeLockService a       = node("A");
        final RecordingListener       aEvents = new RecordingListener();
        final NodeLeader              aLeader = a.registerLeader(LOCK, aEvents);
        await().atMost(WAIT).until(aLeader::isLeader);

        assertThat(aLeader.stepDown(Duration.ofMinutes(5))).isFalse();
        assertThat(aLeader.isLeader()).isTrue();
        sleep(TICK.multipliedBy(2));
        assertThat(aEvents.demoted).isEmpty();
        assertThat(lockHolders()).containsExactly(PostgresNodeLockService.APPLICATION_NAME_PREFIX + "A");
    }

    @Test
    public void stepDownIsRefusedForFollower() {
        final PostgresNodeLockService a       = node("A");
        final NodeLeader              aLeader = a.registerLeader(LOCK, new RecordingListener());
        await().atMost(WAIT).until(aLeader::isLeader);

        final PostgresNodeLockService b       = node("B");
        final NodeLeader              bLeader = b.registerLeader(LOCK, new RecordingListener());
        sleep(TICK.multipliedBy(2));

        assertThat(bLeader.stepDown(Duration.ofSeconds(1))).isFalse();
        assertThat(aLeader.isLeader()).isTrue();
    }

    @Test
    public void slowTicksDemoteOnlyAfterThreeInARow() {
        final PostgresNodeLockService a       = node("A");
        final RecordingListener       aEvents = new RecordingListener();
        final NodeLeader              aLeader = a.registerLeader(LOCK, aEvents);
        await().atMost(WAIT).until(aLeader::isLeader);

        // Two checks in a row that outlast the check timeout: no demotion.
        InstrumentedTestDriver.slowNextQueries(MAX_TIMEOUTS - 1, CHECK_TIMEOUT.plus(TICK.dividedBy(2)));
        sleep(CHECK_TIMEOUT.plus(TICK).multipliedBy(MAX_TIMEOUTS + 2));
        assertThat(aLeader.isLeader()).isTrue();
        assertThat(aEvents.demoted).isEmpty();

        // Three in a row: the connection counts as lost and the leader is demoted.
        InstrumentedTestDriver.slowNextQueries(MAX_TIMEOUTS, CHECK_TIMEOUT.plus(TICK.dividedBy(2)));
        await().atMost(WAIT).until(() -> aEvents.demoted.size() == 1);
        assertThat(aEvents.demoted.get(0)).contains(MAX_TIMEOUTS + " ticks in a row timed out");

        // The connection is reopened on a later tick and, with nobody else around, the leader is re-elected.
        await().atMost(WAIT).until(aLeader::isLeader);
        assertThat(aEvents.elected).hasSize(2);
    }

    @Test
    public void shutdownReleasesImmediately() {
        final PostgresNodeLockService a       = node("A");
        final RecordingListener       aEvents = new RecordingListener();
        final NodeLeader              aLeader = a.registerLeader(LOCK, aEvents);
        await().atMost(WAIT).until(aLeader::isLeader);

        final PostgresNodeLockService b       = node("B");
        final RecordingListener       bEvents = new RecordingListener();
        final NodeLeader              bLeader = b.registerLeader(LOCK, bEvents);
        sleep(TICK.multipliedBy(2));

        a.close();
        assertThat(aLeader.isLeader()).isFalse();
        assertThat(aEvents.demoted).containsExactly("shutdown");
        assertThat(lockHolders()).doesNotContain(PostgresNodeLockService.APPLICATION_NAME_PREFIX + "A");
        await().atMost(WAIT).until(bLeader::isLeader);
    }

    @Test
    public void secondRegistrationForNameOnOneNodeIsRefused() {
        final PostgresNodeLockService a = node("A");
        a.registerLeader(LOCK, new RecordingListener());
        assertThatThrownBy(() -> a.registerLeader(LOCK, new RecordingListener()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(LOCK);
    }

    @Test
    public void configuredDriverIsUsedForFirstConnectionAndReconnect() throws SQLException {
        final PostgresNodeLockService a       = node("A");
        final NodeLeader              aLeader = a.registerLeader(LOCK, new RecordingListener());
        await().atMost(WAIT).until(aLeader::isLeader);
        assertThat(InstrumentedTestDriver.connects()).isEqualTo(1);

        terminateLockConnection("A");
        await().atMost(WAIT).until(() -> !aLeader.isLeader());
        await().atMost(WAIT).until(() -> InstrumentedTestDriver.connects() == 2);
        await().atMost(WAIT).until(aLeader::isLeader);
    }

    @Test
    public void startFailsWhenLockConnectionCannotBeOpened() {
        final NodeLockConnectionSettings badDriver = new NodeLockConnectionSettings("org.nrg.NoSuchDriver", postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        assertThatThrownBy(() -> start(new PostgresNodeLockService("A", true, () -> true, badDriver, TICK, CHECK_TIMEOUT, MAX_TIMEOUTS)))
                .isInstanceOf(NrgServiceRuntimeException.class)
                .hasMessageContaining("lock connection")
                .hasMessageContaining("org.nrg.NoSuchDriver")
                .hasMessageContaining(org.nrg.framework.node.NodeLockService.PROPERTY_ENABLED + "=false");

        final NodeLockConnectionSettings badPassword = new NodeLockConnectionSettings(InstrumentedTestDriver.CLASS_NAME, postgres.getJdbcUrl(), postgres.getUsername(), "wrong");
        assertThatThrownBy(() -> start(new PostgresNodeLockService("A", true, () -> true, badPassword, TICK, CHECK_TIMEOUT, MAX_TIMEOUTS)))
                .isInstanceOf(NrgServiceRuntimeException.class)
                .hasMessageContaining("lock connection");
    }

    @Test
    public void killSwitchOffOpensNothingAndFollowsPrimaryNode() {
        final NodeLockConnectionSettings badDriver = new NodeLockConnectionSettings("org.nrg.NoSuchDriver", postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());

        final PostgresNodeLockService primary      = start(new PostgresNodeLockService("A", false, () -> true, badDriver, TICK, CHECK_TIMEOUT, MAX_TIMEOUTS));
        final RecordingListener       primaryEvents = new RecordingListener();
        final NodeLeader              primaryLeader = primary.registerLeader(LOCK, primaryEvents);

        final PostgresNodeLockService shadow       = start(new PostgresNodeLockService("B", false, () -> false, badDriver, TICK, CHECK_TIMEOUT, MAX_TIMEOUTS));
        final RecordingListener       shadowEvents = new RecordingListener();
        final NodeLeader              shadowLeader = shadow.registerLeader(LOCK, shadowEvents);

        assertThat(primary.isEnabled()).isFalse();
        assertThat(primary.isConnected()).isFalse();
        assertThat(InstrumentedTestDriver.connects()).isZero();
        assertThat(lockHolders()).isEmpty();

        assertThat(primaryLeader.isLeader()).isTrue();
        await().atMost(WAIT).until(() -> primaryEvents.elected.size() == 1);
        assertThat(primaryLeader.stepDown(Duration.ofSeconds(1))).isFalse();
        assertThat(primaryLeader.isLeader()).isTrue();

        sleep(TICK.multipliedBy(3));
        assertThat(shadowLeader.isLeader()).isFalse();
        assertThat(shadowEvents.elected).isEmpty();
        assertThat(shadowLeader.stepDown(Duration.ofSeconds(1))).isFalse();
    }

    @Test
    public void stepDownWhoseUnlockFailsStillDemotesTheLeader() {
        final PostgresNodeLockService a       = node("A");
        final RecordingListener       aEvents = new RecordingListener();
        final NodeLeader              aLeader = a.registerLeader(LOCK, aEvents);
        await().atMost(WAIT).until(aLeader::isLeader);

        final PostgresNodeLockService b       = node("B");
        final RecordingListener       bEvents = new RecordingListener();
        final NodeLeader              bLeader = b.registerLeader(LOCK, bEvents);
        await().atMost(WAIT).until(b::isConnected);

        // The unlock statement fails on the wire. That is a lost connection, so the leader must be demoted and told,
        // not left flagged as a follower with its listener still believing it leads.
        InstrumentedTestDriver.failNextStatements("pg_advisory_unlock", 1);
        assertThat(aLeader.stepDown(Duration.ofSeconds(1))).isFalse();

        assertThat(aLeader.isLeader()).isFalse();
        await().atMost(WAIT).until(() -> aEvents.demoted.size() == 1);
        assertThat(aEvents.demoted.get(0)).startsWith("lock connection lost");

        // Postgres freed the lock with the aborted session; one of the two nodes ends up holding it, and only one.
        await().atMost(WAIT).until(() -> aLeader.isLeader() || bLeader.isLeader());
        await().atMost(WAIT).until(a::isConnected);
        assertThat(lockHolders()).hasSize(1);
        assertThat(aLeader.isLeader() && bLeader.isLeader()).isFalse();
    }

    @Test
    public void lockGrantedOnAConnectionAlreadyDeclaredLostIsNotKept() {
        final PostgresNodeLockService a       = node("A");
        final RecordingListener       aEvents = new RecordingListener();
        final NodeLeader              aLeader = a.registerLeader(LOCK, aEvents);
        await().atMost(WAIT).until(aLeader::isLeader);

        final PostgresNodeLockService b       = node("B");
        final RecordingListener       bEvents = new RecordingListener();
        final NodeLeader              bLeader = b.registerLeader(LOCK, bEvents);
        await().atMost(WAIT).until(b::isConnected);

        // A steps down with a backoff, so its next try-lock is at a known time. B takes the lock and is then closed,
        // which frees it, so A's try-lock after the backoff will be granted.
        final Duration backoff = Duration.ofSeconds(1);
        assertThat(aLeader.stepDown(backoff)).isTrue();
        await().atMost(WAIT).until(bLeader::isLeader);
        b.close();
        assertThat(lockHolders()).isEmpty();

        // That reply is held back long enough for A's tick thread to declare the connection lost and abort it. The
        // grant then arrives on a dead session, whose lock Postgres has already freed; A must not record it.
        final Duration pause = CHECK_TIMEOUT.plus(TICK).multipliedBy(MAX_TIMEOUTS + 2);
        InstrumentedTestDriver.pauseAfterNextQueries("pg_try_advisory_lock", 1, pause);

        await().atMost(WAIT.plus(backoff).plus(pause)).until(() -> !a.isConnected());
        // Without the guard A would have elected on the dead connection, been demoted on the next tick when the lock
        // wasn't held, and elected again: three elections and two demotions instead of two and one.
        await().atMost(WAIT.plus(pause)).until(aLeader::isLeader);
        sleep(TICK.multipliedBy(3));
        assertThat(aEvents.elected).hasSize(2);
        assertThat(aEvents.demoted).containsExactly("stepped down");
        assertThat(lockHolders()).containsExactly(PostgresNodeLockService.APPLICATION_NAME_PREFIX + "A");
    }

    // ---------------------------------------------------------------------------------------------------------------

    private PostgresNodeLockService node(final String nodeId) {
        return start(new PostgresNodeLockService(nodeId, true, () -> true, settings(), TICK, CHECK_TIMEOUT, MAX_TIMEOUTS));
    }

    private PostgresNodeLockService start(final PostgresNodeLockService service) {
        _services.add(service);
        service.start();
        return service;
    }

    private static NodeLockConnectionSettings settings() {
        return new NodeLockConnectionSettings(InstrumentedTestDriver.CLASS_NAME, postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    }

    private static Connection plainConnection() throws SQLException {
        return DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    }

    private static void terminateLockConnection(final String nodeId) throws SQLException {
        try (final Connection connection = plainConnection();
             final PreparedStatement statement = connection.prepareStatement("SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE application_name = ?")) {
            statement.setString(1, PostgresNodeLockService.APPLICATION_NAME_PREFIX + nodeId);
            statement.execute();
        }
    }

    /**
     * The application names of the sessions holding the test lock, from {@code pg_locks}.
     */
    private static List<String> lockHolders() {
        final List<String> holders = new ArrayList<>();
        try (final Connection connection = plainConnection();
             final PreparedStatement statement = connection.prepareStatement("SELECT a.application_name FROM pg_locks l JOIN pg_stat_activity a ON a.pid = l.pid WHERE l.locktype = 'advisory' AND l.classid = ?::oid AND l.objid = hashtext(?)::oid AND l.granted ORDER BY 1")) {
            statement.setInt(1, PostgresNodeLockService.KEY_SPACE);
            statement.setString(2, LOCK);
            try (final ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    holders.add(results.getString(1));
                }
            }
        } catch (SQLException e) {
            throw new AssertionError("Couldn't read pg_locks", e);
        }
        return holders;
    }

    private static void sleep(final Duration duration) {
        try {
            TimeUnit.MILLISECONDS.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static class RecordingListener implements NodeLeaderListener {
        final List<String> elected = new CopyOnWriteArrayList<>();
        final List<String> demoted = new CopyOnWriteArrayList<>();

        @Override
        public void onElected(final String name) {
            elected.add(name);
        }

        @Override
        public void onDemoted(final String name, final String reason) {
            demoted.add(reason);
        }
    }
}
