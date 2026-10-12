package org.nrg.xft.db;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.XDAT;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.context.support.GenericApplicationContext;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A save's update triggers run in pipelines of {@link DBAction#TRIGGER_PIPELINE} statements, each one round trip
 * and one implicit transaction. When any statement in a pipeline fails, the pipeline must leave nothing behind and
 * its statements must then run one at a time, so the failing trigger stops only itself. Each trigger here marks its
 * row and records the transaction it ran in, which shows what ran, how often, and whether it ran in a pipeline.
 * Needs Docker; skipped without it.
 */
public class TriggerPipelineTest {
    private static final int PIPELINE = DBAction.TRIGGER_PIPELINE;
    private static final int TRIGGERS = 2 * PIPELINE + 20;   // two full pipelines and a short third
    private static final int FAILING  = PIPELINE + 25;       // in the middle of the second pipeline

    private static PostgreSQLContainer<?> postgres;
    private static DataSource             dataSource;

    @BeforeClass
    public static void startDatabase() throws Exception {
        Assume.assumeTrue("Docker is needed for the PostgreSQL container", DockerClientFactory.instance().isDockerAvailable());
        postgres = new PostgreSQLContainer<>("postgres:12");
        postgres.start();

        final PGSimpleDataSource pg = new PGSimpleDataSource();
        pg.setUrl(postgres.getJdbcUrl());
        pg.setUser(postgres.getUsername());
        pg.setPassword(postgres.getPassword());
        dataSource = pg;

        // PoolDBUtils takes its connections from XDAT.getDataSource(), which looks the bean up through the
        // ContextService bean of the current context (Spring hands it the context on refresh) and keeps it.
        final GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean(DataSource.class, () -> pg);
        context.registerBean(ContextService.class);
        context.refresh();
        resetXdatDataSource();

        execute("CREATE TABLE trigger_meta (id int PRIMARY KEY, modified int NOT NULL DEFAULT 0)",
                "CREATE TABLE trigger_calls (id int NOT NULL, tx bigint NOT NULL)",
                "INSERT INTO trigger_meta (id) SELECT generate_series(1, " + TRIGGERS + ")",
                "CREATE FUNCTION update_ls_test(item int, usr int) RETURNS varchar AS $$ BEGIN "
                + "UPDATE trigger_meta SET modified = 1 WHERE id = item; "
                + "INSERT INTO trigger_calls VALUES (item, txid_current()); RETURN 'ok'; END; $$ LANGUAGE plpgsql",
                "CREATE FUNCTION update_ls_failing(item int, usr int) RETURNS varchar AS $$ BEGIN "
                + "RAISE EXCEPTION 'trigger % failed', item; END; $$ LANGUAGE plpgsql",
                // An error the tolerant single-command executor logs and swallows (PoolDBUtils.execute).
                "CREATE FUNCTION update_ls_tolerated(item int, usr int) RETURNS varchar AS $$ BEGIN "
                + "PERFORM 1 FROM xdat_meta_element_meta_data; RETURN 'unreachable'; END; $$ LANGUAGE plpgsql");
    }

    @AfterClass
    public static void stopDatabase() throws Exception {
        if (postgres != null) {
            ContextService.getInstance().setApplicationContext(null);
            resetXdatDataSource();
            postgres.stop();
        }
    }

    @Before
    public void clearRows() throws SQLException {
        execute("TRUNCATE trigger_calls", "UPDATE trigger_meta SET modified = 0");
    }

    @Test
    public void pipelinesThatSucceedRunAsOneTransactionEach() throws SQLException {
        DBAction.ExecuteTriggerCommands(commands(-1, null), null, "test");

        final Map<Integer, List<Long>> calls = calls();
        for (int id = 1; id <= TRIGGERS; id++) {
            assertEquals("trigger " + id + " should run exactly once", 1, calls.getOrDefault(id, List.of()).size());
        }
        assertEquals("every pipeline should be one transaction", 3, distinctTransactions(calls, 1, TRIGGERS));
        assertEquals(TRIGGERS, modifiedRows().size());
    }

    @Test
    public void aFailingTriggerStopsOnlyItself() throws SQLException {
        assertFallback("update_ls_failing");
    }

    /**
     * The pipeline has to run through the executor that throws on every error: through the tolerant one, this
     * failure would be logged and swallowed, the server would still roll the pipeline back, and the other triggers
     * in it would be lost without a word.
     */
    @Test
    public void aFailureTheTolerantExecutorSwallowsStillFallsBack() throws SQLException {
        assertFallback("update_ls_tolerated");
    }

    private void assertFallback(final String failingFunction) throws SQLException {
        DBAction.ExecuteTriggerCommands(commands(FAILING, failingFunction), null, "test");

        final Map<Integer, List<Long>> calls    = calls();
        final Set<Integer>             modified = modifiedRows();
        for (int id = 1; id <= TRIGGERS; id++) {
            if (id == FAILING) {
                assertFalse("the failing trigger should have no effect", calls.containsKey(id) || modified.contains(id));
            } else {
                // Exactly once: the statements of the failed pipeline that ran before the failure were rolled back
                // with it, so running them again one at a time does not run them twice.
                assertEquals("trigger " + id + " should run exactly once", 1, calls.getOrDefault(id, List.of()).size());
                assertTrue("trigger " + id + " should have marked its row", modified.contains(id));
            }
        }
        assertEquals("the first pipeline should be one transaction", 1, distinctTransactions(calls, 1, PIPELINE));
        assertEquals("the failed pipeline's triggers should each run on their own", PIPELINE - 1,
                     distinctTransactions(calls, PIPELINE + 1, 2 * PIPELINE));
        assertEquals("the last pipeline should be one transaction", 1, distinctTransactions(calls, 2 * PIPELINE + 1, TRIGGERS));
    }

    private static List<String> commands(final int failing, final String failingFunction) {
        return IntStream.rangeClosed(1, TRIGGERS)
                        .mapToObj(id -> "SELECT %s(%d,7)".formatted(id == failing ? failingFunction : "update_ls_test", id))
                        .collect(Collectors.toList());
    }

    private static Map<Integer, List<Long>> calls() throws SQLException {
        final Map<Integer, List<Long>> calls = new HashMap<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery("SELECT id, tx FROM trigger_calls")) {
            while (results.next()) {
                calls.computeIfAbsent(results.getInt(1), id -> new ArrayList<>()).add(results.getLong(2));
            }
        }
        return calls;
    }

    private static long distinctTransactions(final Map<Integer, List<Long>> calls, final int from, final int to) {
        return IntStream.rangeClosed(from, to)
                        .filter(calls::containsKey)
                        .mapToObj(calls::get)
                        .flatMap(List::stream)
                        .distinct()
                        .count();
    }

    private static Set<Integer> modifiedRows() throws SQLException {
        final Set<Integer> rows = new HashSet<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery("SELECT id FROM trigger_meta WHERE modified = 1")) {
            while (results.next()) {
                rows.add(results.getInt(1));
            }
        }
        return rows;
    }

    private static void execute(final String... sql) throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            for (final String s : sql) {
                statement.execute(s);
            }
        }
    }

    private static void resetXdatDataSource() throws ReflectiveOperationException {
        final Field field = XDAT.class.getDeclaredField("_dataSource");
        field.setAccessible(true);
        field.set(null, null);
    }
}
