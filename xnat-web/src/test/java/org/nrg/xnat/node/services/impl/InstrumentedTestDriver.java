/*
 * web: org.nrg.xnat.node.services.impl.InstrumentedTestDriver
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.node.services.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A JDBC driver for {@link PostgresNodeLockServiceTest}: a non-default driver class, so tests can check that the
 * configured class is the one used, and one that can interfere with connects and statements on demand, so tests can
 * simulate a database that can't be reached, a lock connection that stops answering, a statement that fails, or a
 * reply that arrives after the connection has been given up on.
 */
public class InstrumentedTestDriver extends org.postgresql.Driver {
    public static final String CLASS_NAME = InstrumentedTestDriver.class.getName();

    private static final AtomicInteger CONNECTS      = new AtomicInteger();
    private static final AtomicInteger ATTEMPTS      = new AtomicInteger();
    private static volatile Properties LAST_INFO     = new Properties();
    private static final AtomicInteger LATE_CONNECTS = new AtomicInteger();
    private static volatile Duration   LATE_DURATION = Duration.ZERO;
    private static final AtomicInteger FAIL_CONNECTS = new AtomicInteger();
    private static volatile String     FAIL_STATE    = null;
    private static final AtomicInteger SLOW_QUERIES  = new AtomicInteger();
    private static volatile Duration   SLOW_DURATION = Duration.ZERO;

    private static volatile String     FAIL_SQL   = null;
    private static final AtomicInteger FAIL_COUNT = new AtomicInteger();

    private static volatile String     PAUSE_AFTER_SQL      = null;
    private static final AtomicInteger PAUSE_AFTER_COUNT    = new AtomicInteger();
    private static volatile Duration   PAUSE_AFTER_DURATION = Duration.ZERO;

    public static void reset() {
        CONNECTS.set(0);
        ATTEMPTS.set(0);
        LAST_INFO = new Properties();
        LATE_CONNECTS.set(0);
        LATE_DURATION = Duration.ZERO;
        FAIL_CONNECTS.set(0);
        FAIL_STATE = null;
        SLOW_QUERIES.set(0);
        SLOW_DURATION = Duration.ZERO;
        FAIL_SQL = null;
        FAIL_COUNT.set(0);
        PAUSE_AFTER_SQL = null;
        PAUSE_AFTER_COUNT.set(0);
        PAUSE_AFTER_DURATION = Duration.ZERO;
    }

    public static int connects() {
        return CONNECTS.get();
    }

    /**
     * How many times {@code connect} has been entered, including connects that haven't returned yet.
     */
    public static int connectAttempts() {
        return ATTEMPTS.get();
    }

    /**
     * The properties the most recent {@code connect} was given.
     */
    public static Properties lastConnectProperties() {
        return LAST_INFO;
    }

    /**
     * Makes the next {@code count} connects open the connection and then hold it for the duration before returning
     * it, ignoring interrupts the way a blocking socket connect does. Simulates a connect that succeeds only after
     * its caller has stopped waiting for it.
     */
    public static void delayNextConnects(final int count, final Duration duration) {
        LATE_DURATION = duration;
        LATE_CONNECTS.set(count);
    }

    /**
     * Makes the next {@code count} connects throw an {@link SQLException} with the SQLSTATE instead of connecting.
     * Simulates a database that can't be reached ({@code 08001}) or that refuses the connection.
     */
    public static void failNextConnects(final int count, final String sqlState) {
        FAIL_STATE = sqlState;
        FAIL_CONNECTS.set(count);
    }

    /**
     * Makes the next {@code count} {@code executeQuery} calls on any connection from this driver sleep for the
     * duration before running.
     */
    public static void slowNextQueries(final int count, final Duration duration) {
        SLOW_DURATION = duration;
        SLOW_QUERIES.set(count);
    }

    /**
     * Makes the next {@code count} executions of prepared statements whose SQL contains {@code sqlFragment} throw
     * an {@link SQLException} instead of running.
     */
    public static void failNextStatements(final String sqlFragment, final int count) {
        FAIL_SQL = sqlFragment;
        FAIL_COUNT.set(count);
    }

    /**
     * Makes the next {@code count} {@code executeQuery} calls on prepared statements whose SQL contains
     * {@code sqlFragment} run normally, then sleep for the duration before handing the result back. Simulates a
     * reply that Postgres has already sent, arriving after the caller has decided the connection is dead.
     */
    public static void pauseAfterNextQueries(final String sqlFragment, final int count, final Duration duration) {
        PAUSE_AFTER_SQL      = sqlFragment;
        PAUSE_AFTER_DURATION = duration;
        PAUSE_AFTER_COUNT.set(count);
    }

    @Override
    public Connection connect(final String url, final Properties info) throws SQLException {
        ATTEMPTS.incrementAndGet();
        final Properties received = new Properties();
        received.putAll(info);
        LAST_INFO = received;
        if (FAIL_CONNECTS.get() > 0 && FAIL_CONNECTS.decrementAndGet() >= 0) {
            throw new SQLException("Instrumented connect failure", FAIL_STATE);
        }
        final Connection connection = super.connect(url, info);
        if (connection == null) {
            return null;
        }
        if (LATE_CONNECTS.get() > 0 && LATE_CONNECTS.decrementAndGet() >= 0) {
            final long deadline = System.nanoTime() + LATE_DURATION.toNanos();
            for (long left = LATE_DURATION.toNanos(); left > 0; left = deadline - System.nanoTime()) {
                try {
                    TimeUnit.NANOSECONDS.sleep(left);
                } catch (InterruptedException ignored) {
                    // Keep holding the connection, as a blocking connect would.
                }
            }
        }
        CONNECTS.incrementAndGet();
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Connection.class}, new Delegating(connection, null));
    }

    private static boolean isExecute(final Method method) {
        final String name = method.getName();
        return "execute".equals(name) || "executeQuery".equals(name) || "executeUpdate".equals(name);
    }

    private static boolean matches(final String sql, final String fragment) {
        return sql != null && fragment != null && sql.contains(fragment);
    }

    /**
     * Proxies a connection ({@code sql == null}) or a prepared statement (with the SQL it was prepared with).
     */
    private static class Delegating implements InvocationHandler {
        private final Object _target;
        private final String _sql;

        Delegating(final Object target, final String sql) {
            _target = target;
            _sql    = sql;
        }

        private boolean isStatement() {
            return _sql != null;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if (isStatement() && isExecute(method)) {
                if (matches(_sql, FAIL_SQL) && FAIL_COUNT.get() > 0 && FAIL_COUNT.decrementAndGet() >= 0) {
                    throw new SQLException("Instrumented failure for: " + _sql);
                }
                if ("executeQuery".equals(method.getName()) && SLOW_QUERIES.get() > 0 && SLOW_QUERIES.decrementAndGet() >= 0) {
                    Thread.sleep(SLOW_DURATION.toMillis());
                }
            }
            final Object result;
            try {
                result = method.invoke(_target, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
            if (isStatement() && "executeQuery".equals(method.getName())
                && matches(_sql, PAUSE_AFTER_SQL) && PAUSE_AFTER_COUNT.get() > 0 && PAUSE_AFTER_COUNT.decrementAndGet() >= 0) {
                Thread.sleep(PAUSE_AFTER_DURATION.toMillis());
            }
            if (!isStatement() && result instanceof PreparedStatement) {
                final String sql = args != null && args.length > 0 && args[0] instanceof String ? (String) args[0] : "";
                return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{PreparedStatement.class}, new Delegating(result, sql));
            }
            return result;
        }
    }
}
