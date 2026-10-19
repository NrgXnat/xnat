/*
 * web: org.nrg.xnat.node.services.impl.PostgresNodeLockService
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.node.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.node.NodeLeader;
import org.nrg.framework.node.NodeLeaderListener;
import org.nrg.framework.node.NodeLockService;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

/**
 * {@link NodeLockService} on Postgres session-level advisory locks.
 * <p>
 * Every node opens <b>one dedicated lock connection</b>, directly through the configured JDBC driver rather than
 * from the application pool, and keeps it for its lifetime. Advisory locks belong to that session: they are released
 * when it ends, however it ends, so a node that dies or loses its connection loses its locks without any cleanup.
 * The connection identifies itself as {@code xnat-locks:<node.id>} in {@code pg_stat_activity}.
 * <p>
 * All work on the connection runs on one <b>lock thread</b>, because JDBC connections aren't thread-safe. A
 * <b>tick thread</b> submits a check every {@link #DEFAULT_TICK_INTERVAL tick}: it confirms that the leader locks
 * this node holds are still granted and lets registered leaders that don't lead try to take a free lock. A check
 * that fails with a database error means the connection is gone and every leader on this node is demoted at once.
 * A check that merely doesn't answer within the {@link #DEFAULT_CHECK_TIMEOUT check timeout} only counts against
 * the connection after {@link #DEFAULT_MAX_TIMEOUTS three} in a row, so a busy node or a slow database doesn't
 * demote everything for nothing. After the third, the connection is aborted and every leader is demoted. A lost
 * connection is reopened on the next tick, as is one that couldn't be opened at startup because the database couldn't
 * be reached.
 * <p>
 * Listener callbacks run on a <b>callback thread</b>, in order and never on the lock thread, so a slow
 * {@link NodeLeaderListener#onDemoted(String, String)} can't stall other lock operations.
 * <p>
 * Lock keys are {@code pg_try_advisory_lock(0x584E, hashtext(name))} in the two-int keyspace, so they can't collide
 * with the single-bigint {@code hashtext} keys other XNAT code already uses.
 * <p>
 * With the kill switch {@link NodeLockService#PROPERTY_ENABLED} off, no connection is opened and a leader leads if
 * and only if this node is the primary node, which is the behavior before this service existed.
 */
@Slf4j
public class PostgresNodeLockService implements NodeLockService, AutoCloseable {
    /**
     * The first int of every advisory lock key this service takes ("XN"). It appears as {@code classid} 22606 in
     * {@code pg_locks}.
     */
    public static final int      KEY_SPACE               = 0x584E;
    public static final String   APPLICATION_NAME_PREFIX = "xnat-locks:";
    public static final Duration DEFAULT_TICK_INTERVAL   = Duration.ofSeconds(10);
    public static final Duration DEFAULT_CHECK_TIMEOUT   = Duration.ofSeconds(5);
    public static final int      DEFAULT_MAX_TIMEOUTS    = 3;

    private static final String SQL_SET_APPLICATION_NAME = "SELECT set_config('application_name', ?, false)";
    private static final String SQL_TRY_LOCK             = "SELECT pg_try_advisory_lock(?, hashtext(?)), hashtext(?)";
    private static final String SQL_UNLOCK               = "SELECT pg_advisory_unlock(?, ?)";
    private static final String SQL_HELD_KEYS            = "SELECT objid FROM pg_locks WHERE locktype = 'advisory' AND classid = ?::oid AND objsubid = 2 AND pid = pg_backend_pid() AND granted";
    // Advisory locks are per database, so only lock sessions on this database are nodes that could take over.
    private static final String SQL_OTHER_NODES          = "SELECT count(*) FROM pg_stat_activity WHERE application_name LIKE ? AND datname = current_database() AND pid <> pg_backend_pid()";

    private final String                     _nodeId;
    private final boolean                    _enabled;
    private final BooleanSupplier            _primaryNode;
    private final NodeLockConnectionSettings _settings;
    private final Duration                   _tickInterval;
    private final Duration                   _checkTimeout;
    private final int                        _maxTimeouts;

    private final Map<String, Leader> _leaders = new ConcurrentHashMap<>();

    private final ExecutorService          _lockExecutor;
    private final ScheduledExecutorService _tickExecutor;
    private final ExecutorService          _callbackExecutor;

    private volatile Connection _connection;
    private final AtomicBoolean _connected = new AtomicBoolean();
    /**
     * Guards the promotion in {@link #tryElect} against the demotion loop in {@link #connectionLost}, which runs on
     * the tick thread. Without it a lock granted on a connection that the tick thread has just aborted would be
     * recorded as held, and this node would believe it leads until the next tick noticed.
     */
    private final Object        _leadership = new Object();
    private final AtomicBoolean _started   = new AtomicBoolean();
    private final AtomicBoolean _closed    = new AtomicBoolean();

    // Only touched on the tick thread.
    private Future<?> _pendingTick;
    private int       _timeouts;

    /**
     * Creates the service with the production tick interval, check timeout and timeout count.
     *
     * @param nodeId      This node's ID, from {@code node.id}.
     * @param enabled     The value of {@link NodeLockService#PROPERTY_ENABLED}.
     * @param primaryNode Whether this node is the primary node. Only consulted when the service is disabled.
     * @param settings    How to open the lock connection.
     */
    public PostgresNodeLockService(final String nodeId, final boolean enabled, final BooleanSupplier primaryNode, final NodeLockConnectionSettings settings) {
        this(nodeId, enabled, primaryNode, settings, DEFAULT_TICK_INTERVAL, DEFAULT_CHECK_TIMEOUT, DEFAULT_MAX_TIMEOUTS);
    }

    /**
     * Creates the service with explicit timings. Tests use this to run in milliseconds.
     *
     * @param nodeId       This node's ID, from {@code node.id}.
     * @param enabled      The value of {@link NodeLockService#PROPERTY_ENABLED}.
     * @param primaryNode  Whether this node is the primary node. Only consulted when the service is disabled.
     * @param settings     How to open the lock connection.
     * @param tickInterval How often the lock connection is checked and free leader locks are tried.
     * @param checkTimeout How long a check may take before it counts as timed out.
     * @param maxTimeouts  How many timed-out checks in a row count as a lost connection.
     */
    public PostgresNodeLockService(final String nodeId, final boolean enabled, final BooleanSupplier primaryNode, final NodeLockConnectionSettings settings, final Duration tickInterval, final Duration checkTimeout, final int maxTimeouts) {
        _nodeId       = StringUtils.defaultIfBlank(nodeId, "UNCONFIGURED");
        _enabled      = enabled;
        _primaryNode  = primaryNode;
        _settings     = settings;
        _tickInterval = tickInterval;
        _checkTimeout = checkTimeout;
        _maxTimeouts  = Math.max(1, maxTimeouts);

        _lockExecutor     = Executors.newSingleThreadExecutor(runnable -> daemon("xnat-locks", runnable));
        _tickExecutor     = Executors.newSingleThreadScheduledExecutor(runnable -> daemon("xnat-locks-tick", runnable));
        _callbackExecutor = Executors.newSingleThreadExecutor(runnable -> daemon("xnat-locks-callbacks", runnable));
    }

    /**
     * Opens the lock connection and starts the tick. With the service disabled, nothing is opened. If the database
     * can't be reached yet, the node starts anyway and the tick keeps trying to open the connection, as it does after
     * a lost connection.
     *
     * @throws NrgServiceRuntimeException When the lock connection can't be opened because of a configuration error,
     *                                    such as a driver that can't be loaded or bad credentials. The node must not
     *                                    serve with one: it would never lead, and only log lines would say why.
     */
    public void start() {
        if (!_started.compareAndSet(false, true)) {
            return;
        }
        if (!_enabled) {
            log.warn("Node locks are disabled by {}=false on node {}: no lock connection will be opened and leaders will elect themselves only on the primary node", PROPERTY_ENABLED, _nodeId);
            return;
        }
        try {
            _lockExecutor.submit(() -> {
                openConnection();
                return null;
            }).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Interrupted while opening the node lock connection");
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            if (!isUnreachableDatabase(cause)) {
                throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "Could not open the node lock connection for node " + _nodeId + " with driver " + _settings.getDriverClassName() + " and URL " + urlForMessages() + ". XNAT can't run node locks without it. Fix the datasource configuration, or set " + PROPERTY_ENABLED + "=false to start without node locks: " + cause.getMessage(), cause);
            }
            log.warn("Node locks on node {}: could not open the lock connection ({}), will retry on every tick. No leader on this node can be elected until it opens.", _nodeId, cause.getMessage());
        }
        _tickExecutor.scheduleWithFixedDelay(this::tick, _tickInterval.toMillis(), _tickInterval.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isEnabled() {
        return _enabled;
    }

    /**
     * Indicates whether the lock connection is currently open. For status reporting.
     *
     * @return {@code true} when the lock connection is open.
     */
    public boolean isConnected() {
        return _connected.get();
    }

    public String getNodeId() {
        return _nodeId;
    }

    @Override
    public NodeLeader registerLeader(final String name, final NodeLeaderListener listener) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("A leader lock needs a name");
        }
        if (listener == null) {
            throw new IllegalArgumentException("A leader lock needs a listener");
        }
        final Leader leader = new Leader(name, listener);
        if (_leaders.putIfAbsent(name, leader) != null) {
            throw new IllegalStateException("A leader is already registered for lock \"" + name + "\" on node " + _nodeId + ". Postgres would grant the lock to both, so a second registration on one node is refused.");
        }
        if (!_enabled) {
            if (_primaryNode.getAsBoolean()) {
                leader._leader = true;
                log.info("Node lock \"{}\": elected on node {} because node locks are disabled and this is the primary node", name, _nodeId);
                callback(() -> listener.onElected(name));
            } else {
                log.info("Node lock \"{}\": node {} will never lead because node locks are disabled and this is not the primary node", name, _nodeId);
            }
            return leader;
        }
        if (_started.get() && !_closed.get()) {
            try {
                _lockExecutor.execute(() -> electOnLockThread(leader));
            } catch (RejectedExecutionException ignored) {
                // Closing.
            }
        }
        return leader;
    }

    /**
     * Demotes every leader, releases every lock and closes the lock connection. Called by Spring at shutdown.
     */
    @Override
    public void close() {
        if (!_closed.compareAndSet(false, true)) {
            return;
        }
        _tickExecutor.shutdownNow();
        if (!_enabled) {
            // No lock connection, but the primary node leads all the same, and its listeners are owed the same
            // "shutting down" demotion as with locks on.
            for (final Leader leader : _leaders.values()) {
                demote(leader, "shutdown");
            }
        } else if (_started.get()) {
            try {
                _lockExecutor.submit(this::shutdownOnLockThread).get(_checkTimeout.toMillis() * 2, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                log.warn("Node locks on node {}: the lock thread didn't shut down within {}, aborting the lock connection", _nodeId, _checkTimeout.multipliedBy(2));
                connectionLost("shutdown while the lock thread was blocked");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                log.warn("Node locks on node {}: error during shutdown", _nodeId, e.getCause());
            }
        }
        _lockExecutor.shutdownNow();
        _callbackExecutor.shutdown();
        try {
            if (!_callbackExecutor.awaitTermination(_checkTimeout.toMillis() * 2, TimeUnit.MILLISECONDS)) {
                log.warn("Node locks on node {}: leader callbacks didn't finish within {} at shutdown", _nodeId, _checkTimeout.multipliedBy(2));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Tick thread
    // ---------------------------------------------------------------------------------------------------------------

    private void tick() {
        if (_closed.get()) {
            return;
        }
        try {
            if (_pendingTick == null || _pendingTick.isDone()) {
                _pendingTick = _lockExecutor.submit(this::tickOnLockThread);
            }
            _pendingTick.get(_checkTimeout.toMillis(), TimeUnit.MILLISECONDS);
            _timeouts = 0;
        } catch (TimeoutException e) {
            if (!_connected.get()) {
                // A reconnect is taking its time. The driver's connect timeout bounds it; there's nothing to demote.
                return;
            }
            _timeouts++;
            log.warn("Node locks on node {}: tick timeout {}/{}, the lock connection hasn't answered within {}", _nodeId, _timeouts, _maxTimeouts, _checkTimeout);
            if (_timeouts >= _maxTimeouts) {
                _timeouts = 0;
                connectionLost(_maxTimeouts + " ticks in a row timed out");
            }
        } catch (ExecutionException e) {
            // runSql has already handled a database error; anything else is a bug worth seeing.
            _timeouts = 0;
            final Throwable cause = e.getCause();
            if (!(cause instanceof SQLException)) {
                log.error("Node locks on node {}: unexpected error on the lock thread", _nodeId, cause);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (RejectedExecutionException ignored) {
            // Closing.
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Lock thread
    // ---------------------------------------------------------------------------------------------------------------

    private Void tickOnLockThread() throws SQLException {
        if (_closed.get()) {
            return null;
        }
        if (_connection == null) {
            try {
                openConnection();
            } catch (SQLException e) {
                log.warn("Node locks on node {}: lock connection could not be opened ({}), will retry on the next tick", _nodeId, e.getMessage());
                return null;
            }
            if (_closed.get()) {
                // close() ran while this connect was in flight and may have stopped waiting for it, dropping its
                // queued shutdown, so nothing else would close this connection. Close it before it can take a lock.
                closeConnection(_connection);
                return null;
            }
            log.info("Node locks on node {}: lock connection restored", _nodeId);
        }
        runSql("tick", connection -> {
            final Set<Integer> held = heldKeys(connection);
            final long         now  = System.nanoTime();
            for (final Leader leader : _leaders.values()) {
                if (leader._leader) {
                    if (!held.contains(leader._key)) {
                        demote(leader, "lock no longer held by this session");
                    }
                } else if (now - leader._backoffUntil >= 0) {
                    tryElect(connection, leader);
                }
            }
            return null;
        });
        return null;
    }

    private void electOnLockThread(final Leader leader) {
        if (_connection == null) {
            return;
        }
        try {
            runSql("elect \"" + leader._name + "\"", connection -> {
                tryElect(connection, leader);
                return null;
            });
        } catch (SQLException e) {
            log.debug("Node lock \"{}\": first election attempt failed, the next tick will retry", leader._name, e);
        }
    }

    private void tryElect(final Connection connection, final Leader leader) throws SQLException {
        if (leader._leader) {
            return;
        }
        try (final PreparedStatement statement = connection.prepareStatement(SQL_TRY_LOCK)) {
            statement.setInt(1, KEY_SPACE);
            statement.setString(2, leader._name);
            statement.setString(3, leader._name);
            try (final ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    throw new SQLException("pg_try_advisory_lock returned no row");
                }
                final boolean granted = results.getBoolean(1);
                leader._key = results.getInt(2);
                if (granted) {
                    synchronized (_leadership) {
                        if (!_connected.get() || _connection != connection) {
                            // The tick thread declared this connection lost while the query was in flight. Postgres
                            // frees the lock with the session, so there is nothing to hold; the next tick retries.
                            log.info("Node lock \"{}\": granted on node {} but the lock connection was lost meanwhile, not electing", leader._name, _nodeId);
                            return;
                        }
                        leader._leader = true;
                        // Queued under the monitor, as demotions are, so a connectionLost() that runs right after
                        // this grant can't queue its onDemoted ahead of this onElected.
                        log.info("Node lock \"{}\": elected on node {}", leader._name, _nodeId);
                        callback(() -> leader._listener.onElected(leader._name));
                    }
                }
            }
        }
    }

    private Set<Integer> heldKeys(final Connection connection) throws SQLException {
        final Set<Integer> keys = new HashSet<>();
        try (final PreparedStatement statement = connection.prepareStatement(SQL_HELD_KEYS)) {
            statement.setInt(1, KEY_SPACE);
            try (final ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    keys.add((int) results.getLong(1));
                }
            }
        }
        return keys;
    }

    private boolean stepDownOnLockThread(final Leader leader, final Duration backoff) throws SQLException {
        if (!leader._leader) {
            return false;
        }
        final long backoffNanos = backoff.toNanos();
        return runSql("step down \"" + leader._name + "\"", connection -> {
            if (!otherNodeConnected(connection)) {
                log.info("Node lock \"{}\": step-down refused on node {}: no other node is connected", leader._name, _nodeId);
                return false;
            }
            // Release first. If this throws, runSql reports the connection lost and connectionLost() demotes the
            // leader (still flagged) with its callback; clearing the flag beforehand would skip that and leave the
            // listener believing it leads.
            unlock(connection, leader);
            final boolean wasLeader;
            synchronized (_leadership) {
                // connectionLost() can demote the leader, and tell its listener, while the unlock is in flight.
                wasLeader            = clearLeader(leader);
                leader._backoffUntil = System.nanoTime() + backoffNanos;
            }
            if (wasLeader) {
                log.info("Node lock \"{}\": stepped down on node {} (backoff {})", leader._name, _nodeId, backoff);
                callback(() -> leader._listener.onDemoted(leader._name, "stepped down"));
            }
            return true;
        });
    }

    private boolean otherNodeConnected(final Connection connection) throws SQLException {
        try (final PreparedStatement statement = connection.prepareStatement(SQL_OTHER_NODES)) {
            statement.setString(1, APPLICATION_NAME_PREFIX + "%");
            try (final ResultSet results = statement.executeQuery()) {
                return results.next() && results.getLong(1) > 0;
            }
        }
    }

    private void unlock(final Connection connection, final Leader leader) throws SQLException {
        try (final PreparedStatement statement = connection.prepareStatement(SQL_UNLOCK)) {
            statement.setInt(1, KEY_SPACE);
            statement.setInt(2, leader._key);
            statement.execute();
        }
    }

    private void shutdownOnLockThread() {
        final Connection connection = _connection;
        for (final Leader leader : _leaders.values()) {
            if (clearLeader(leader)) {
                if (connection != null) {
                    try {
                        unlock(connection, leader);
                    } catch (SQLException e) {
                        log.debug("Node lock \"{}\": couldn't release on shutdown, closing the connection releases it anyway", leader._name, e);
                    }
                }
                log.info("Node lock \"{}\": demoted on node {} (shutdown)", leader._name, _nodeId);
                callback(() -> leader._listener.onDemoted(leader._name, "shutdown"));
            }
        }
        closeConnection(connection);
    }

    /**
     * Marks the lock connection closed and closes it.
     */
    private void closeConnection(final Connection connection) {
        _connected.set(false);
        _connection = null;
        closeQuietly(connection);
    }

    private void openConnection() throws SQLException {
        final Driver     driver     = loadDriver();
        // The pool's driver properties first, so the credentials and the lock connection's own settings below win.
        final Properties properties = _settings.getDriverProperties();
        if (_settings.getUsername() != null) {
            properties.setProperty("user", _settings.getUsername());
        }
        if (_settings.getPassword() != null) {
            properties.setProperty("password", _settings.getPassword());
        }
        properties.setProperty("ApplicationName", APPLICATION_NAME_PREFIX + _nodeId);
        properties.setProperty("tcpKeepAlive", "true");
        final String timeoutSeconds = Long.toString(Math.max(1, _checkTimeout.getSeconds() * 2));
        properties.setProperty("connectTimeout", timeoutSeconds);
        properties.setProperty("loginTimeout", timeoutSeconds);

        final Connection connection = driver.connect(_settings.getUrl(), properties);
        if (connection == null) {
            throw new SQLNonTransientConnectionException("Driver " + _settings.getDriverClassName() + " doesn't accept the URL " + urlForMessages());
        }
        try {
            connection.setAutoCommit(true);
            try (final PreparedStatement statement = connection.prepareStatement(SQL_SET_APPLICATION_NAME)) {
                statement.setString(1, APPLICATION_NAME_PREFIX + _nodeId);
                statement.execute();
            }
        } catch (SQLException e) {
            closeQuietly(connection);
            throw e;
        }
        _connection = connection;
        _connected.set(true);
        log.info("Node locks on node {}: lock connection opened as {}{} with driver {}", _nodeId, APPLICATION_NAME_PREFIX, _nodeId, _settings.getDriverClassName());
    }

    /**
     * Indicates whether a failure to open the lock connection can clear by itself, so the node should start and
     * retry rather than refuse to start. That is a database that can't be reached (SQLSTATE class 08, which the
     * Postgres driver also reports for an unknown host and a connect timeout), one that is out of resources such as
     * connection slots (class 53), or one that is still starting up (57P03). Anything else is a configuration error
     * that retrying won't fix, such as a driver that can't be loaded or doesn't accept the URL (no SQLSTATE), bad
     * credentials (class 28) or a missing database (3D000).
     */
    private static boolean isUnreachableDatabase(final Throwable cause) {
        if (!(cause instanceof SQLException)) {
            return false;
        }
        final String state = ((SQLException) cause).getSQLState();
        return state != null && (state.startsWith("08") || state.startsWith("53") || "57P03".equals(state));
    }

    /**
     * The URL without its query string, for log and error messages: the query string can carry the password
     * ({@code ?password=...}).
     */
    private String urlForMessages() {
        return StringUtils.substringBefore(_settings.getUrl(), "?");
    }

    private Driver loadDriver() throws SQLException {
        try {
            return Class.forName(_settings.getDriverClassName()).asSubclass(Driver.class).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new SQLNonTransientConnectionException("Couldn't load the JDBC driver class " + _settings.getDriverClassName() + ": " + e, e);
        }
    }

    /**
     * Runs an operation on the lock connection. A database error means the connection is gone: every leader is
     * demoted and the connection is dropped before the error is rethrown.
     */
    private <T> T runSql(final String what, final SqlOperation<T> operation) throws SQLException {
        final Connection connection = _connection;
        if (connection == null) {
            throw new SQLNonTransientConnectionException("The lock connection is not open");
        }
        try {
            return operation.apply(connection);
        } catch (SQLException e) {
            connectionLost(what + ": " + e.getMessage());
            throw e;
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Any thread
    // ---------------------------------------------------------------------------------------------------------------

    /**
     * Records that the lock connection is gone: demotes every leader and drops the connection. Safe to call from
     * any thread, including while the lock thread is blocked on the connection, and idempotent.
     */
    private void connectionLost(final String reason) {
        if (!_connected.compareAndSet(true, false)) {
            return;
        }
        log.warn("Node locks on node {}: lock connection lost ({})", _nodeId, reason);
        final Connection connection = _connection;
        _connection = null;
        abortQuietly(connection);
        final String demotion = "lock connection lost: " + reason;
        synchronized (_leadership) {
            for (final Leader leader : _leaders.values()) {
                demote(leader, demotion);
            }
        }
    }

    private void demote(final Leader leader, final String reason) {
        if (!clearLeader(leader)) {
            return;
        }
        log.info("Node lock \"{}\": demoted on node {} ({})", leader._name, _nodeId, reason);
        callback(() -> leader._listener.onDemoted(leader._name, reason));
    }

    /**
     * Clears the leader flag under the leadership monitor and reports whether it was set, so that when several paths
     * demote the same leader at once, only one of them calls the listener.
     */
    private boolean clearLeader(final Leader leader) {
        synchronized (_leadership) {
            final boolean wasLeader = leader._leader;
            leader._leader = false;
            return wasLeader;
        }
    }

    private void callback(final Runnable runnable) {
        try {
            _callbackExecutor.execute(() -> {
                try {
                    runnable.run();
                } catch (Throwable t) {
                    log.error("Node locks on node {}: a leader listener threw", _nodeId, t);
                }
            });
        } catch (RejectedExecutionException e) {
            log.debug("Node locks on node {}: callback dropped, the service is closed", _nodeId);
        }
    }

    private void abortQuietly(final Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            // Closes the socket from this thread, which unblocks a lock thread stuck in a read. JDBC 4.1.
            connection.abort(Runnable::run);
        } catch (Throwable t) {
            log.debug("Node locks on node {}: abort failed, closing instead", _nodeId, t);
            closeQuietly(connection);
        }
    }

    private static void closeQuietly(final Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (Throwable ignored) {
            // Nothing to do.
        }
    }

    private <T> T onLockThread(final String what, final Callable<T> callable, final T fallback) {
        final Future<T> future;
        try {
            future = _lockExecutor.submit(callable);
        } catch (RejectedExecutionException e) {
            return fallback;
        }
        try {
            return future.get(_checkTimeout.toMillis() * 2, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            // The caller acts on the fallback, so the operation mustn't run later behind its back. One that has
            // already started can't be stopped and completes as usual.
            future.cancel(false);
            log.warn("Node locks on node {}: {} didn't complete within {}", _nodeId, what, _checkTimeout.multipliedBy(2));
            return fallback;
        } catch (ExecutionException e) {
            log.warn("Node locks on node {}: {} failed", _nodeId, what, e.getCause());
            return fallback;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return fallback;
        }
    }

    private static Thread daemon(final String name, final Runnable runnable) {
        final Thread thread = new Thread(runnable, name);
        thread.setDaemon(true);
        return thread;
    }

    @FunctionalInterface
    private interface SqlOperation<T> {
        T apply(Connection connection) throws SQLException;
    }

    private class Leader implements NodeLeader {
        private final String             _name;
        private final NodeLeaderListener _listener;
        private volatile boolean         _leader;
        private volatile long            _backoffUntil = System.nanoTime();
        private volatile int             _key;

        Leader(final String name, final NodeLeaderListener listener) {
            _name     = name;
            _listener = listener;
        }

        @Override
        public String getName() {
            return _name;
        }

        @Override
        public boolean isLeader() {
            return _leader;
        }

        @Override
        public boolean stepDown(final Duration backoff) {
            if (!_enabled) {
                log.info("Node lock \"{}\": step-down refused on node {}: node locks are disabled", _name, _nodeId);
                return false;
            }
            if (!_leader || _closed.get()) {
                return false;
            }
            return onLockThread("step down \"" + _name + "\"", () -> stepDownOnLockThread(this, backoff), false);
        }

        @Override
        public String toString() {
            return "NodeLeader{" + _name + " on " + _nodeId + (_leader ? ", leader}" : "}");
        }
    }
}
